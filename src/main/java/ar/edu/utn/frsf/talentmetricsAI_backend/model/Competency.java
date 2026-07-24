package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import java.time.LocalDateTime;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.CompetencyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "competencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetencyType type;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
