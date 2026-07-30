package ar.edu.utn.frsf.talentmetricsAI_backend.dto.competency;

import java.util.List;

import lombok.Data;

@Data
public class PositionSelectDTO {
    private Long id;
    private String name;
    private String company;
    private List<CompetencyCountDTO> competencies;

    public PositionSelectDTO(Long id, String name, String company, List<CompetencyCountDTO> competencies) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.competencies = competencies;
    }
}
