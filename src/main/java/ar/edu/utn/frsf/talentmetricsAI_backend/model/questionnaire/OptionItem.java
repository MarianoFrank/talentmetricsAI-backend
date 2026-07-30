package ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Option;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "option_items")
public class OptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_item_id", nullable = false)
    private QuestionItem questionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;

    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered = false;
}
