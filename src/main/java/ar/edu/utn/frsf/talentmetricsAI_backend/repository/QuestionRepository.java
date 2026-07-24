package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Question;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Cuenta cuántas preguntas tiene un factor específico
    long countByFactorId(Long factorId);

    @Query("SELECT new ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionSummaryResponse(" +
            "q.id, q.updatedAt, c.name, f.name, q.name) " +
            "FROM Question q " +
            "JOIN q.factor f " +
            "JOIN f.competency c " +
            "WHERE q.deletedAt IS NULL " +
            "AND (:competencyId IS NULL OR c.id = :competencyId) " +
            "AND (:factorId IS NULL OR f.id = :factorId) " +
            "AND (:questionName IS NULL OR LOWER(q.name) LIKE LOWER(CONCAT('%', CAST(:questionName AS string), '%')))")
    Page<QuestionSummaryResponse> findAllSummaryQuestionsWithFilters(
            @Param("competencyId") Long competencyId,
            @Param("factorId") Long factorId,
            @Param("questionName") String questionName,
            Pageable pageable);

    // Le decimos que en esta consulta en particular, NO sea perezoso con "options"
    // Esto evita hacer dos consultas a la base de datos (una para la pregunta y
    // otra para las opciones)
    @EntityGraph(attributePaths = { "options" })
    Optional<Question> findByIdAndDeletedAtIsNull(Long id);
}
