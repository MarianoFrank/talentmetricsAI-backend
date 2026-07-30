package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation.EvaluationKeyResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.evaluation.GenerateEvaluationRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<EvaluationKeyResponse>> generate(@RequestBody GenerateEvaluationRequest request) {
        List<EvaluationKeyResponse> response = evaluationService.generateEvaluation(request);
        return ResponseEntity.ok(response);
    }
}
