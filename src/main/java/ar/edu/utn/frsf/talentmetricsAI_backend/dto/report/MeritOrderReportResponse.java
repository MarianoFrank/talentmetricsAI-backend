package ar.edu.utn.frsf.talentmetricsAI_backend.dto.report;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeritOrderReportResponse {
    private String companyName;
    private String positionName;
    private String printedBy;
    private LocalDateTime printedAt;
    private List<ReportCandidateDto> approvedCandidates;
    private List<ReportCandidateDto> rejectedOrIncompleteCandidates;

}
