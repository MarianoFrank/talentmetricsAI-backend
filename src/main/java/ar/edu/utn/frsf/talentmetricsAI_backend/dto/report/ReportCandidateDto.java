package ar.edu.utn.frsf.talentmetricsAI_backend.dto.report;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportCandidateDto {
    private String firstName;
    private String lastName;
    private String docType;
    private String docNumber;
    private String candidateNumber;
    private String state;
    private Double score;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer accessCount;
}
