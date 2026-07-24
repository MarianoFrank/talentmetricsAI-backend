package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateCsvDTO;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Candidate;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.DocumentType;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.Gender;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CandidateRepository;
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

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
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
            // Inicializamos el mapper de Jackson para CSV
            CsvMapper csvMapper = new CsvMapper();
            // Le decimos que no rompa si el CSV trae columnas extra que no mapeamos
            csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // Le indicamos que la primera línea es la cabecera
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            // Mapeamos todo el archivo directamente a nuestra lista de DTOs.
            // Al pasarle file.getInputStream(), Jackson limpia el BOM solo.
            MappingIterator<CandidateCsvDTO> iterator = csvMapper.readerFor(CandidateCsvDTO.class)
                    .with(schema)
                    .readValues(file.getInputStream());

            List<CandidateCsvDTO> csvCandidates = iterator.readAll();

            // Iteramos sobre objetos Java puros, sin ensuciarnos con CSVRecords
            for (CandidateCsvDTO csvDto : csvCandidates) {
                Long candidateNumber = Long.parseLong(csvDto.getCandidateNumber());

                Candidate candidate = candidateRepository.findByCandidateNumber(candidateNumber)
                        .orElseGet(() -> {
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

                            return candidateRepository.save(newCandidate);
                        });

                processedCandidates.add(new CandidateSummaryResponse(
                        candidate.getId(), candidate.getFirstName(), candidate.getLastName(),
                        candidate.getCandidateNumber()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage());
        }

        return processedCandidates;
    }
}
