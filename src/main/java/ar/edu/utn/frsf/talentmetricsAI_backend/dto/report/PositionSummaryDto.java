package ar.edu.utn.frsf.talentmetricsAI_backend.dto.report;

public record PositionSummaryDto(
        Long id,
        String code,
        String positionName,
        String companyName,
        int totalCandidates,
        int completedEvaluations) {
}
