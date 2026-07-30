package ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Factor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "factor_scores")
public class FactorScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_score_id", nullable = false)
    private CompetencyScore competencyScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id", nullable = false)
    private Factor factor;

    @Column(name = "score", nullable = false)
    private Double score;
}
