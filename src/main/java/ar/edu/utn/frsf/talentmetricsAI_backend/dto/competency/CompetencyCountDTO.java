package ar.edu.utn.frsf.talentmetricsAI_backend.dto.competency;

import lombok.Data;

@Data
public class CompetencyCountDTO {
    private String name;
    private Integer weightingRequired;
    // si la competencia cumple con que los factores tenga al menos 2 preguntas
    private boolean meetsCondition;

    public CompetencyCountDTO(String name, Integer weightingRequired, boolean meetsCondition) {
        this.name = name;
        this.weightingRequired = weightingRequired;
        this.meetsCondition = meetsCondition;
    }

}
