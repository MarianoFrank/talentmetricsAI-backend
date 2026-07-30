package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

//Llave compuesta para la relación muchos a muchos entre Puesto y Competencia
@Embeddable
@Data
@EqualsAndHashCode
public class PositionCompetencyId implements Serializable {

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "competency_id")
    private Long competencyId;

}
