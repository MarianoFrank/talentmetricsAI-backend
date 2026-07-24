package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.FactorService;

@RestController
@RequestMapping("/api/factors")
public class FactorController {

    private final FactorService factorService;

    public FactorController(FactorService factorService) {
        this.factorService = factorService;
    }

    @GetMapping("/select")
    public ResponseEntity<List<SelectItemResponse>> getFactorsForSelect(
            @RequestParam(required = false) Long competencyId) {
        List<SelectItemResponse> factors = factorService.getFactorsForSelect(competencyId);
        return ResponseEntity.ok(factors);
    }
}
