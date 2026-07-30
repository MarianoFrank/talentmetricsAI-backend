package ar.edu.utn.frsf.talentmetricsAI_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsultantProfile {
    private String username;
    private String role;
    private String name;
    private String lastName;
    private String legajo;
}
