package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
