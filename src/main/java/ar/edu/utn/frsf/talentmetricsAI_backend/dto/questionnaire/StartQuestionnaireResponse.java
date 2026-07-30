package ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartQuestionnaireResponse {
    private Long questionnaireId;
    private int totalBlocks;
    private int currentBlock;
    private int durationMinutes;
    private String state;
    private LocalDateTime startedAt;
}
