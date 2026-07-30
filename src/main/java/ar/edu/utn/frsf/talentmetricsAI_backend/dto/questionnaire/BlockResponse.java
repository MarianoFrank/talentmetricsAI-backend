package ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class BlockResponse {
    private Long id;
    private List<QuestionItemResponse> questionItems;
}
