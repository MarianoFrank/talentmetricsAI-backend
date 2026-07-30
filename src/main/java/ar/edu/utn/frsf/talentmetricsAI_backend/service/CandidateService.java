package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateCsvDTO;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Candidate;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.DocumentType;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.Gender;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CandidateRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.security.JwtService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository, QuestionnaireRepository questionnaireRepository,
            JwtService jwtService) {
        this.candidateRepository = candidateRepository;
    }

    public Page<CandidateSummaryResponse> getPaginatedCandidates(String firstName, String lastName,
            Long candidateNumber,
            Pageable pageable) {
        return candidateRepository.findSummaryByFilters(firstName, lastName, candidateNumber, pageable);
    }

    @Transactional
    public List<CandidateSummaryResponse> processCsvCandidates(MultipartFile file) {
        List<CandidateSummaryResponse> processedCandidates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            CsvMapper csvMapper = new CsvMapper();
            csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            MappingIterator<CandidateCsvDTO> iterator = csvMapper.readerFor(CandidateCsvDTO.class)
                    .with(schema)
                    .readValues(file.getInputStream());

            List<CandidateCsvDTO> csvCandidates = iterator.readAll();

            // 1. Extraemos todos los números de candidato del CSV de una pasada
            List<Long> candidateNumbers = csvCandidates.stream()
                    .map(dto -> Long.parseLong(dto.getCandidateNumber()))
                    .collect(Collectors.toList());

            // 2. Buscamos en la base de datos TODOS los que ya existen con una sola
            // consulta
            List<Candidate> existingCandidates = candidateRepository.findByCandidateNumberIn(candidateNumbers);

            // 3. Armamos un diccionario en memoria (Map) para buscar rapidísimo sin tocar
            // la DB
            Map<Long, Candidate> existingMap = existingCandidates.stream()
                    .collect(Collectors.toMap(c -> c.getCandidateNumber(), c -> c));

            List<Candidate> newCandidatesToSave = new ArrayList<>();

            // 4. Recorremos el CSV separando los candidatos nuevos de los viejos
            for (CandidateCsvDTO csvDto : csvCandidates) {
                Long candidateNumber = Long.parseLong(csvDto.getCandidateNumber());

                if (existingMap.containsKey(candidateNumber)) {
                    // Ya existe: lo agregamos directamente a la lista de respuesta
                    Candidate existing = existingMap.get(candidateNumber);
                    processedCandidates.add(new CandidateSummaryResponse(
                            existing.getId(), existing.getFirstName(), existing.getLastName(),
                            existing.getCandidateNumber()));
                } else {
                    // Es nuevo: armamos el objeto pero NO lo guardamos todavía
                    Candidate newCandidate = new Candidate();
                    newCandidate.setCandidateNumber(candidateNumber);
                    newCandidate.setDocumentType(DocumentType.valueOf(csvDto.getDocumentType().toUpperCase()));
                    newCandidate.setDocumentNumber(csvDto.getDocumentNumber());
                    newCandidate.setFirstName(csvDto.getFirstName());
                    newCandidate.setLastName(csvDto.getLastName());
                    newCandidate.setBirthDate(LocalDate.parse(csvDto.getBirthDate(), formatter));
                    newCandidate.setGender(Gender.valueOf(csvDto.getGender().toUpperCase()));
                    newCandidate.setEmail(csvDto.getEmail());

                    if (csvDto.getEducationLevel() != null && !csvDto.getEducationLevel().isBlank()) {
                        newCandidate.setEducationLevel(csvDto.getEducationLevel());
                    }
                    if (csvDto.getNationality() != null && !csvDto.getNationality().isBlank()) {
                        newCandidate.setNationality(csvDto.getNationality());
                    }

                    // Lo mandamos a la lista de espera
                    newCandidatesToSave.add(newCandidate);
                }
            }

            // 5. Guardamos todos los candidatos nuevos en un solo saque usando saveAll
            if (!newCandidatesToSave.isEmpty()) {
                List<Candidate> savedCandidates = candidateRepository.saveAll(newCandidatesToSave);
                for (Candidate saved : savedCandidates) {
                    processedCandidates.add(new CandidateSummaryResponse(
                            saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getCandidateNumber()));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando el archivo CSV: " + e.getMessage());
        }

        // Devolvemos la lista completa (los que ya estaban + los recién creados)
        return processedCandidates;
    }

    public Candidate findByDocumentNumber(String documentNumber) {
        return candidateRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(
                        () -> new RuntimeException("Candidato con documento " + documentNumber + " no encontrado"));
    }
}
