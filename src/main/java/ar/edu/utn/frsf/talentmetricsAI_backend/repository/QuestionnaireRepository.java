package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Questionnaire;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    Optional<Questionnaire> findByAccessKey(String accessKey);

    @Query("SELECT q FROM Questionnaire q WHERE q.evaluation.closeDate < :now AND q.state IN (:states)")
    List<Questionnaire> findExpiredQuestionnaires(
            @Param("now") LocalDateTime now,
            @Param("states") List<QuestionnaireState> states);

    // Trae los cuestionarios de una evaluación particular
    List<Questionnaire> findByEvaluationId(Long evaluationId);

    // Trae los cuestionarios de TODAS las evaluaciones de un puesto particular
    List<Questionnaire> findByEvaluationPositionId(Long positionId);
}
