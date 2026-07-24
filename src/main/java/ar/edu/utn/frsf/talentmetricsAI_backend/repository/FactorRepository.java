package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Factor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FactorRepository extends JpaRepository<Factor, Long> {

    @Query("""
                SELECT new ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse(f.id, f.name)
                FROM Factor f
                ORDER BY f.name
            """)
    List<SelectItemResponse> findAllForSelect();

    // NUEVO: Consulta para filtrar los factores que pertenecen a una competencia
    @Query("""
                SELECT new ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse(f.id, f.name)
                FROM Factor f
                WHERE f.competency.id = :competencyId
                ORDER BY f.name
            """)
    List<SelectItemResponse> findForSelectByCompetencyId(@Param("competencyId") Long competencyId);
}
