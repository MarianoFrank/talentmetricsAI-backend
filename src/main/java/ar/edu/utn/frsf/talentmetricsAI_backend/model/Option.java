package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lazy significa que no se cargará automáticamente la pregunta asociada a la
    // opción, sino que se cargará solo cuando se acceda a ella.
    // Siempre que el metodo sea transaccional (no cierre la sesión de hibernate)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
}
