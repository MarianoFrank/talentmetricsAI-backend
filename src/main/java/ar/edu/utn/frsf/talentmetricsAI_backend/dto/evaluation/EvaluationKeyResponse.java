package ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation;

import lombok.Data;

@Data
public class EvaluationKeyResponse {
    private String candidateNumber;
    private String firstName;
    private String lastName;
    private String accessKey;

    public EvaluationKeyResponse(String candidateNumber, String firstName, String lastName, String accessKey) {
        this.candidateNumber = candidateNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accessKey = accessKey;
    }

}
