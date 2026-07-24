package ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate;

public record CandidateSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        Long candidateNumber) {
}
