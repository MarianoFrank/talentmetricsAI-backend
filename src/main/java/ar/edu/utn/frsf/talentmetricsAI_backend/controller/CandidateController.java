package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.CandidateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ResponseEntity<Page<CandidateSummaryResponse>> getCandidates(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long candidateNumber,
            Pageable pageable) {

        return ResponseEntity
                .ok(candidateService.getPaginatedCandidates(firstName, lastName, candidateNumber, pageable));
    }

    @PostMapping("/import")
    public ResponseEntity<List<CandidateSummaryResponse>> importCandidatesCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Ahora el tipo coincide perfecto con el servicio
        List<CandidateSummaryResponse> candidates = candidateService.processCsvCandidates(file);
        return ResponseEntity.ok(candidates);
    }
}
