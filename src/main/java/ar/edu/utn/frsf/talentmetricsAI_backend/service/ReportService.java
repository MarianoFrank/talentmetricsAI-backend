package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.EvaluationSummaryDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.MeritOrderReportResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.PositionSummaryDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.ReportCandidateDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Questionnaire;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.EvaluationRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.PositionRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    private final QuestionnaireRepository questionnaireRepository;
    private final PositionRepository positionRepository;
    private final EvaluationRepository evaluationRepository;
    private final ScoringAsyncService scoringService;

    public ReportService(QuestionnaireRepository questionnaireRepository, PositionRepository positionRepository,
            EvaluationRepository evaluationRepository, ScoringAsyncService scoringService) {
        this.questionnaireRepository = questionnaireRepository;
        this.positionRepository = positionRepository;
        this.evaluationRepository = evaluationRepository;
        this.scoringService = scoringService;
    }

    @Transactional(readOnly = true)
    public MeritOrderReportResponse generateMeritOrder(Long positionId, Long evaluationId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado"));

        List<Questionnaire> questionnaires;

        // Validamos que la evaluación exista y que pertenezca al puesto
        if (evaluationId != null) {
            Evaluation evaluation = evaluationRepository.findById(evaluationId)
                    .orElseThrow(() -> new IllegalArgumentException("La evaluación especificada no existe"));

            if (!evaluation.getPosition().getId().equals(positionId)) {
                throw new IllegalArgumentException("La evaluación enviada no pertenece al puesto seleccionado");
            }

            questionnaires = questionnaireRepository.findByEvaluationId(evaluationId);
        } else {
            questionnaires = questionnaireRepository.findByEvaluationPositionId(positionId);
        }

        List<ReportCandidateDto> approved = new ArrayList<>();
        List<ReportCandidateDto> rejected = new ArrayList<>();

        for (Questionnaire q : questionnaires) {
            // Si está completado pero el servidor se cayó antes de
            // calcular el puntaje
            if (q.getState() == QuestionnaireState.COMPLETED && q.getTotalScore() == null) {
                // Lo calculamos a la fuerza en este momento
                Double puntajeRecuperado = scoringService.calcularPuntajeSincrono(q.getId());
                q.setTotalScore(puntajeRecuperado);
            }

            Candidate c = q.getCandidate();

            // Mapeamos los datos adicionales según si completó o no
            LocalDateTime finalDate = (q.getState() == QuestionnaireState.COMPLETED) ? q.getEndedAt()
                    : q.getLastAccess();

            ReportCandidateDto candidateDto = new ReportCandidateDto(
                    c.getFirstName(),
                    c.getLastName(),
                    c.getDocumentType().name(),
                    c.getDocumentNumber(),
                    String.valueOf(c.getCandidateNumber()),
                    q.getState().name(),
                    (q.getState() == QuestionnaireState.COMPLETED) ? q.getTotalScore() : null,
                    q.getStartedAt(),
                    finalDate,
                    q.getAccessCount());

            if (q.getState() == QuestionnaireState.COMPLETED && meetsMinimumRequirements(q, position)) {
                approved.add(candidateDto);
            } else {
                rejected.add(candidateDto);
            }
        }

        approved.sort(Comparator.comparing(dto -> dto.getScore(), Comparator.reverseOrder()));
        rejected.sort(Comparator.comparing(dto -> dto.getState()));

        // Sacamos el nombre del consultor que lo está emitiendo
        String consultor = SecurityContextHolder.getContext().getAuthentication().getName();

        return new MeritOrderReportResponse(
                position.getCompany().getName(),
                position.getName(),
                consultor,
                LocalDateTime.now(),
                approved,
                rejected);
    }

    // Valida si el candidato alcanzó las ponderaciones mínimas en TODAS las
    // competencias
    private boolean meetsMinimumRequirements(Questionnaire q, Position p) {
        for (PositionCompetency pc : p.getCompetencies()) {
            Long compId = pc.getCompetency().getId();

            double minRequired = pc.getWeightingRequired();

            // Buscamos cuánto sacó el candidato en esta competencia específica
            double candidateScore = q.getCompetencyScores().stream()
                    .filter(cs -> cs.getCompetency().getId().equals(compId))
                    .map(cs -> cs.getScore())
                    .findFirst()
                    .orElse(0.0);

            if (candidateScore < minRequired) {
                return false; // Rebotó en al menos una, afuera del orden de mérito
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Page<PositionSummaryDto> getPositionsForReport(Long companyId, String positionName, String code,
            Pageable pageable) {

        Page<Position> positions = positionRepository.findWithFilters(companyId, positionName, code, pageable);

        return positions.map(pos -> {
            // Traemos todos los cuestionarios de este puesto
            List<Questionnaire> qs = questionnaireRepository.findByEvaluationPositionId(pos.getId());

            int totalCandidates = qs.size();
            int completed = (int) qs.stream().filter(q -> q.getState() == QuestionnaireState.COMPLETED).count();

            return new PositionSummaryDto(
                    pos.getId(),
                    pos.getCode(),
                    pos.getName(),
                    pos.getCompany().getName(),
                    totalCandidates,
                    completed);
        });
    }

    @Transactional(readOnly = true)
    public List<EvaluationSummaryDto> getEvaluationsSummaryByPosition(Long positionId) {

        List<Evaluation> evaluations = evaluationRepository.findByPositionId(positionId);
        List<EvaluationSummaryDto> dtoList = new ArrayList<>();

        for (Evaluation eval : evaluations) {
            // Traemos los cuestionarios solo de esta evaluación
            List<Questionnaire> qs = questionnaireRepository.findByEvaluationId(eval.getId());

            int total = qs.size();
            int completados = (int) qs.stream().filter(q -> q.getState() == QuestionnaireState.COMPLETED).count();

            // Formateamos la fecha
            String fechaApertura = eval.getCreatedAt() != null
                    ? eval.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                    : "N/A";

            // Armamos el string exacto que pide tu diseño
            String description = String.format("%s - Candidatos:%d - Evaluaciones Completadas:%d",
                    fechaApertura, total, completados);

            dtoList.add(new EvaluationSummaryDto(eval.getId(), description));
        }

        return dtoList;
    }

}
