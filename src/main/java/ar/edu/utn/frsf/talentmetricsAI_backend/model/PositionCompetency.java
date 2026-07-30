package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PositionCompetency {

    @EmbeddedId
    private PositionCompetencyId id = new PositionCompetencyId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("positionId")
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("competencyId")
    @JoinColumn(name = "competency_id")
    private Competency competency;

    private Integer weightingRequired;

}
