package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.MeritOrderReportResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('CONSULTANT')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Endpoint: GET /api/reports/merit-order/positions/8?evaluationId=15
    // Si no mandan el evaluationId, asume "TODAS".
    @GetMapping("/merit-order/positions/{positionId}")
    public ResponseEntity<MeritOrderReportResponse> getMeritOrder(
            @PathVariable Long positionId,
            @RequestParam(required = false) Long evaluationId) {

        MeritOrderReportResponse response = reportService.generateMeritOrder(positionId, evaluationId);
        return ResponseEntity.ok(response);
    }
}
