package ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionItemResponse {
    private Long id;
    private int displayOrder;
    private String text;
    private boolean isMultiple;
    private List<OptionItemResponse> optionItems;
}
