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
}
