package ar.edu.utn.frsf.talentmetricsAI_backend.dto.ai;

import java.util.List;

public record GenerateQuestionResponse(
        String questionName,
        String description,
        String type,
        String text,
        List<GeneratedOptionResponse> options) {
}
