package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionDetailResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@Validated
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // Endpoint Liviano (para la tabla principal)
    @GetMapping
    public ResponseEntity<Page<QuestionSummaryResponse>> getAllQuestions(
            @RequestParam(required = false) Long competencyId,
            @RequestParam(required = false) Long factorId,
            @RequestParam(required = false) String questionName,
            Pageable pageable) { // <-- Spring captura page, size y sort de la URL automáticamente

        Page<QuestionSummaryResponse> questionsPage = questionService.getAllQuestionsSummary(competencyId, factorId,
                questionName, pageable);
        return ResponseEntity.ok(questionsPage);
    }

    // Endpoint Detallado (para popular el formulario al modificar)
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestionById(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        QuestionDetailResponse questionDetail = questionService.getQuestionById(id);
        return ResponseEntity.ok(questionDetail);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuestion(@Valid @RequestBody QuestionRequest requestDTO) {
        questionService.createQuestion(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Pregunta creada correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @Valid @RequestBody QuestionRequest requestDTO) {
        questionService.updateQuestion(id, requestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "Pregunta actualizada correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {

        questionService.deleteQuestion(id);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "Pregunta dada de baja correctamente"));
    }
}
