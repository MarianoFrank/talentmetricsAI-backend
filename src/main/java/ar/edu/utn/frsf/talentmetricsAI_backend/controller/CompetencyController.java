package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.CompetencyService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/competencies")
public class CompetencyController {

    private final CompetencyService competencyService;

    public CompetencyController(CompetencyService competencyService) {
        this.competencyService = competencyService;
    }

    @GetMapping("/select")
    public ResponseEntity<List<SelectItemResponse>> getAllCompetenciesForSelect() {
        List<SelectItemResponse> competencies = competencyService.getAllCompetenciesForSelect();
        return ResponseEntity.ok(competencies);
    }

}
