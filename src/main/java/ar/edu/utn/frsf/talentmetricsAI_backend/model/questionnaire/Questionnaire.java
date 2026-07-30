package ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Candidate;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Evaluation;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "questionnaires")
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "access_key", nullable = false, unique = true)
    private String accessKey;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // NO es cuando finaliza el cuestionario, sino hasta cuando el candidato puede
    // acceder a él.
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Column(name = "access_count", nullable = false)
    private Integer accessCount = 0;

    @Column(name = "total_score")
    private Double totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private QuestionnaireState state = QuestionnaireState.ACTIVE;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    // Aseguramos que los bloques estén ordenados por su número, importante para el
    // metodo calcularBloqueActual()
    @OrderBy("blockNumber ASC")
    private List<Block> blocks = new ArrayList<>();

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetencyScore> competencyScores = new ArrayList<>();
}
