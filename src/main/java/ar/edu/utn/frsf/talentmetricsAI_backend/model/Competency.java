package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.CompetencyType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "competencies")
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

    @OneToMany(mappedBy = "competency", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Factor> factors = new java.util.ArrayList<>();
}
