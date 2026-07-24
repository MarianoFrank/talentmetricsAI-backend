package ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CandidateCsvDTO {

    @JsonProperty("nro_candidato")
    private String candidateNumber;

    @JsonProperty("tipo_documento")
    private String documentType;

    @JsonProperty("nro_documento")
    private String documentNumber;

    @JsonProperty("nombre")
    private String firstName;

    @JsonProperty("apellido")
    private String lastName;

    @JsonProperty("fecha_nacimiento")
    private String birthDate;

    @JsonProperty("genero")
    private String gender;

    @JsonProperty("email")
    private String email;

    @JsonProperty("escolaridad")
    private String educationLevel;

    @JsonProperty("nacionalidad")
    private String nationality;

    // Generá los Getters y Setters acá (o metele un @Data si usás Lombok)

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public String getNationality() {
        return nationality;
    }
}
