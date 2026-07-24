package ar.edu.utn.frsf.talentmetricsAI_backend.dto.question;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record QuestionSummaryResponse(
        Long id,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm") LocalDateTime updatedAt,
        String competencyName,
        String factorName,
        String questionName) {
}
