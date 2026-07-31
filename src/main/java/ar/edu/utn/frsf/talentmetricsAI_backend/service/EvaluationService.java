package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation.EvaluationKeyResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation.GenerateEvaluationRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Questionnaire;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private final PositionRepository positionRepository;
    private final CandidateRepository candidateRepository;
    private final CompetencyRepository competencyRepository;
    private final EvaluationRepository evaluationRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final ConsultantRepository consultantRepository;

    // Constantes para la clave aleatoria
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public EvaluationService(PositionRepository positionRepository, CandidateRepository candidateRepository,
            CompetencyRepository competencyRepository, EvaluationRepository evaluationRepository,
            QuestionnaireRepository questionnaireRepository, ConsultantRepository consultantRepository) {
        this.positionRepository = positionRepository;
        this.candidateRepository = candidateRepository;
        this.competencyRepository = competencyRepository;
        this.evaluationRepository = evaluationRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.consultantRepository = consultantRepository;
    }

    @Transactional
    public List<EvaluationKeyResponse> generateEvaluation(GenerateEvaluationRequest request) {

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new RuntimeException("Puesto no encontrado"));

        // validamos si las competencias del puesto cumplen con los requisitos para ser
        // evaluadas
        List<Long> validCompIds = competencyRepository.findValidCompetencyIds();
        for (PositionCompetency pc : position.getCompetencies()) {
            if (!validCompIds.contains(pc.getCompetency().getId())) {
                throw new RuntimeException("Error: La competencia " + pc.getCompetency().getName()
                        + " no cumple los requisitos para ser evaluada.");
            }
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setPosition(position);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Consultant consultor = consultantRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos"));

        evaluation.setConsultant(consultor);
        evaluation.setCode("EVAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Le sumamos 7 días pero clavamos la hora a las 23:59:59 para sincronizar con
        // el scheduler
        evaluation.setCloseDate(LocalDateTime.now().plusDays(7).with(LocalTime.MAX));
        evaluation.setDuration(60); // 60 minutos de tiempo para el test

        evaluation = evaluationRepository.save(evaluation);

        // 4. Buscar candidatos y preparar los cuestionarios
        List<Candidate> candidates = candidateRepository.findAllById(request.getCandidateIds());
        List<Questionnaire> questionnaires = new ArrayList<>();
        List<EvaluationKeyResponse> responseList = new ArrayList<>();

        for (Candidate candidate : candidates) {
            Questionnaire q = new Questionnaire();
            q.setEvaluation(evaluation);
            q.setCandidate(candidate);

            // Generar clave de 8 caracteres alfanuméricos
            String accessKey = generateAccessKey();
            q.setAccessKey(accessKey);

            q.setAccessCount(0);
            q.setState(QuestionnaireState.ACTIVE);

            questionnaires.add(q);

            // Llenamos la lista para devolverle al Frontend y que arme el Excel
            responseList.add(new EvaluationKeyResponse(
                    String.valueOf(candidate.getCandidateNumber()),
                    candidate.getFirstName(),
                    candidate.getLastName(),
                    accessKey));
        }

        // Aca no generamos los bloques eso se hace cuando un candidato ingresa con su
        // clave ahi se procesa la generacion y seleccion de preguntas

        questionnaireRepository.saveAll(questionnaires);

        return responseList;
    }

    private String generateAccessKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
