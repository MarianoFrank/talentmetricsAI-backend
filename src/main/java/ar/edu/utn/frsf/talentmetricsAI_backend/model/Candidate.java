package ar.edu.utn.frsf.talentmetricsAI_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.DocumentType;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.Gender;

@Entity
@Data
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nro_candidato", unique = true, nullable = false)
    private Long candidateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private DocumentType documentType;

    @Column(name = "nro_documento", nullable = false)
    private String documentNumber;

    @Column(name = "nombre", nullable = false)
    private String firstName;

    @Column(name = "apellido", nullable = false)
    private String lastName;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String email;

    @Column(name = "escolaridad")
    private String educationLevel;

    @Column(name = "nacionalidad")
    private String nationality;

}
