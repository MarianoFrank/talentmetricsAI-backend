package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.EvaluationSummaryDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.MeritOrderReportResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.report.PositionSummaryDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.ReportService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Endpoint para la grilla paginada y filtrada
    @GetMapping("/merit-order/positions")
    public ResponseEntity<Page<PositionSummaryDto>> getPositionsForMeritOrder(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String code,
            Pageable pageable) {

        Page<PositionSummaryDto> response = reportService.getPositionsForReport(companyId, positionName, code,
                pageable);
        return ResponseEntity.ok(response);
    }

    // Endpoint auxiliar para llenar el dropdown del modal
    @GetMapping("/merit-order/positions/{positionId}/evaluations")
    public ResponseEntity<List<EvaluationSummaryDto>> getEvaluationsForModal(@PathVariable Long positionId) {

        List<EvaluationSummaryDto> response = reportService.getEvaluationsSummaryByPosition(positionId);
        return ResponseEntity.ok(response);
    }
}
