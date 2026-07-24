package ar.edu.utn.frsf.talentmetricsAI_backend.dto.ai;

public record GenerateQuestionRequest(
        String competencyName,
        String factorName,
        String questionName,
        String description,
        String extraContext) {
}
