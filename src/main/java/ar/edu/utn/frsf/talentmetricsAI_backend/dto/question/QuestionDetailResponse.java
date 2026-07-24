package ar.edu.utn.frsf.talentmetricsAI_backend.dto.question;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.option.OptionDetailResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionType;
import java.util.List;

public record QuestionDetailResponse(
        Long id,
        Long factorId,
        String name,
        String text,
        String description,
        QuestionType type,
        List<OptionDetailResponse> options) {
}
