package ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Competency;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "competency_scores")
public class CompetencyScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(name = "score", nullable = false)
    private Double score;

    // Relación hacia los puntajes individuales de cada factor de esta competencia
    @OneToMany(mappedBy = "competencyScore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FactorScore> factorScores = new ArrayList<>();
}
