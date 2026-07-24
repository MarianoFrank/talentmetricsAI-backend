package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.ai.GenerateQuestionRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.ai.GenerateQuestionResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.AiGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGenerationService aiService;

    public AiController(AiGenerationService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-question")
    public ResponseEntity<GenerateQuestionResponse> generateQuestion(@RequestBody GenerateQuestionRequest request) {
        return ResponseEntity.ok(aiService.generateQuestion(request));
    }
}
