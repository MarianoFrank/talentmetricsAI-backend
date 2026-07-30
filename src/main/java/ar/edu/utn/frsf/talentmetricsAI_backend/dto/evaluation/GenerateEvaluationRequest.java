package ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation;

import java.util.List;

import lombok.Data;

@Data
public class GenerateEvaluationRequest {
    private Long positionId;
    private List<Long> candidateIds;

}
