package ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Question;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "question_items")
public class QuestionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)

    private Block block;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "obtained_score")
    private Double obtainedScore;

    @OneToMany(mappedBy = "questionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItems;

}
