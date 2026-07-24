package ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate;

public record CandidateResponse(
        Long id,
        Long candidateNumber,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String email) {
}
