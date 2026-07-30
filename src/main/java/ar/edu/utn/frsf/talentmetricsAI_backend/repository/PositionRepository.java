package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Position;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    // Traemos los puestos junto con sus empresas y competencias para evitar el
    // problema N+1
    @Query("SELECT DISTINCT p FROM Position p " +
            "JOIN FETCH p.company " +
            "LEFT JOIN FETCH p.competencies cp " +
            "LEFT JOIN FETCH cp.competency " +
            "WHERE p.deletedAt IS NULL")
    List<Position> findAllActivePositions();

    @Query("SELECT p FROM Position p WHERE " +
            "(:companyId IS NULL OR p.company.id = :companyId) AND " +
            "(:positionName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:positionName AS string), '%'))) AND "
            +
            "(:code IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')))")
    Page<Position> findWithFilters(
            @Param("companyId") Long companyId,
            @Param("positionName") String positionName,
            @Param("code") String code,
            Pageable pageable);
}
