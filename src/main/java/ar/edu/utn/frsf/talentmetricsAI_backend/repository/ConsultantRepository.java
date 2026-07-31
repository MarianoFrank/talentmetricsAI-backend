package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConsultantRepository extends JpaRepository<Consultant, Integer> {
    Optional<Consultant> findByUsername(String username);

    boolean existsByUsername(String username);
}
