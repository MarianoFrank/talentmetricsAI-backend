package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Competency;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {

    @Query("""
                SELECT new ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse(c.id, c.name)
                FROM Competency c
                ORDER BY c.name
            """)
    List<SelectItemResponse> findAllForSelect();

    // Trae SOLO los IDs de las competencias que tienen al menos un factor con 2 o
    // más preguntas
    @Query(value = "SELECT f.competency_id FROM questions q " +
            "INNER JOIN factors f ON q.factor_id = f.id " +
            "WHERE f.deleted_at IS NULL AND q.deleted_at IS NULL " +
            "GROUP BY f.competency_id, f.id " +
            "HAVING COUNT(q.id) >= 2", nativeQuery = true)
    List<Long> findValidCompetencyIds();
}
