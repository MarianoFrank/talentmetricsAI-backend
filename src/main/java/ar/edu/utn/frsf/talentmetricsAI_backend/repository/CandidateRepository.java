package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByCandidateNumber(Long candidateNumber);

    // Proyección directa a DTO para la grilla
    @Query("SELECT new ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateSummaryResponse(" +
            "c.id, c.firstName, c.lastName, c.candidateNumber) " +
            "FROM Candidate c " +
            "WHERE (:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%'))) "
            +
            "AND (:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS string), '%'))) " +
            "AND (:candidateNumber IS NULL OR c.candidateNumber = :candidateNumber)")
    Page<CandidateSummaryResponse> findSummaryByFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("candidateNumber") Long candidateNumber,
            Pageable pageable);
}
