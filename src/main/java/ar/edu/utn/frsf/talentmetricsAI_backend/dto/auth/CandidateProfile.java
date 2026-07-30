package ar.edu.utn.frsf.talentmetricsAI_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CandidateProfile {
    private String username;
    private String role;
    private String firstName;
    private String lastName;
}
