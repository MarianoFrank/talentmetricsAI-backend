package ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionItemResponse {
    private Long id;
    private int displayOrder;
    private String text;
    private Boolean isAnswered;
}
