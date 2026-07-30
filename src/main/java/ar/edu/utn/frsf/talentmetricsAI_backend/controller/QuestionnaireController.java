package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.BlockResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.StartQuestionnaireResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.QuestionnaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("@securityService.isCandidateOwner(#id)")
    public ResponseEntity<StartQuestionnaireResponse> startQuestionnaire(@PathVariable Long id) {
        StartQuestionnaireResponse q = questionnaireService.startQuestionnaire(id);
        return ResponseEntity.ok(q);
    }

    @GetMapping("/{id}/blocks/{blockNumber}")
    @PreAuthorize("@securityService.isCandidateOwner(#id)")
    public ResponseEntity<BlockResponse> getBlock(@PathVariable Long id, @PathVariable int blockNumber) {
        BlockResponse block = questionnaireService.getBlockByNumber(id, blockNumber);
        return ResponseEntity.ok(block);
    }

    /*
     * Formato de request esperado:
     * {
     * "1": [2, 3], // id de la pregunta : [id de las opciones seleccionadas]
     * "2": [5],
     * "3": []
     * }
     *
     */
    @PostMapping("/{id}/blocks/{blockNumber}")
    @PreAuthorize("@securityService.isCandidateOwner(#id)")
    public ResponseEntity<?> submitBlock(
            @PathVariable Long id,
            @PathVariable int blockNumber,
            @RequestBody Map<String, List<Long>> rawAnswers) {

        // Mapeamos a Long
        Map<Long, List<Long>> answers = new HashMap<>();
        rawAnswers.forEach((key, value) -> answers.put(Long.valueOf(key), value));

        System.out.println("Respuestas recibidas para el bloque " + blockNumber + ": " + answers);

        questionnaireService.submitBlockAnswers(id, blockNumber, answers);
        return ResponseEntity.ok(Map.of("message", "Bloque guardado con éxito"));
    }
}
