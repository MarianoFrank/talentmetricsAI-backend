package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Evaluation;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Block;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.OptionItem;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.QuestionItem;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Questionnaire;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.EvaluationRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.QuestionnaireService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mock")
// BORRAR O COMENTAR ESTA CLASE EN PRODUCCIÓN. SÓLO PARA TESTEO DE FLUJOS
// COMPLETOS.
public class MockDataController {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireService questionnaireService;
    private final EvaluationRepository evaluationRepository;

    public MockDataController(QuestionnaireRepository questionnaireRepository,
            QuestionnaireService questionnaireService,
            EvaluationRepository evaluationRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionnaireService = questionnaireService;
        this.evaluationRepository = evaluationRepository;
    }

    // Responde automaticamente al azar a todos los cuestionarios de una evaluación,
    // simulando que algunos candidatos lo completan, otros lo dejan a medias y
    // otros ni entran.
    @PostMapping("/answer-all/{evaluationId}")
    @Transactional
    public String answerAllRandomly(@PathVariable Long evaluationId) {

        // 1. Buscamos la evaluación para poder cerrarla al final
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluación no encontrada"));

        List<Questionnaire> questionnaires = questionnaireRepository.findByEvaluationId(evaluationId);
        Random random = new Random();

        int countCompletos = 0;
        int countIncompletos = 0;
        int countIgnorados = 0;

        // --- INICIA LA SIMULACIÓN DE CANDIDATOS ---
        for (Questionnaire q : questionnaires) {
            if (q.getState() == QuestionnaireState.ACTIVE) {

                int decision = random.nextInt(100);

                // CASO 1: 20% ni entran al cuestionario
                if (decision < 20) {
                    countIgnorados++;
                    continue;
                }

                questionnaireService.startQuestionnaire(q.getId());
                // IMPORTANTE: Debemos volver a buscar el cuestionario actualizado desde la
                // base de datos, porque el método startQuestionnaire() es una transacción
                // separada y el objeto q que tenemos en memoria no tiene los cambios
                // persistidos.
                Questionnaire updatedQ = questionnaireRepository.findById(q.getId()).orElseThrow();
                List<Block> bloquesCopia = new ArrayList<>(updatedQ.getBlocks());

                int bloquesACompletar = bloquesCopia.size();

                // CASO 2: 30% lo deja por la mitad
                if (decision >= 20 && decision < 50 && bloquesCopia.size() > 1) {
                    bloquesACompletar = random.nextInt(bloquesCopia.size() - 1) + 1;
                    countIncompletos++;
                } else {
                    // CASO 3: 50% lo hace completo
                    countCompletos++;
                }

                for (int i = 0; i < bloquesACompletar; i++) {
                    Block block = bloquesCopia.get(i);
                    Map<Long, List<Long>> answers = new HashMap<>();
                    List<QuestionItem> itemsCopia = new ArrayList<>(block.getQuestionItems());

                    for (QuestionItem item : itemsCopia) {
                        List<OptionItem> options = new ArrayList<>(item.getOptionItems());
                        List<Long> selectedIds = new ArrayList<>();

                        boolean isMultiple = item.getQuestion().getType() != null &&
                                item.getQuestion().getType().name().toUpperCase().contains("MULTIPLE");

                        if (isMultiple) {
                            // Seleccionamos al azar entre 1 y todas las opciones disponibles
                            int cantOpciones = random.nextInt(options.size()) + 1;
                            Collections.shuffle(options);
                            for (int j = 0; j < cantOpciones; j++) {
                                selectedIds.add(options.get(j).getId());
                            }
                        } else {
                            OptionItem randomOption = options.get(random.nextInt(options.size()));
                            selectedIds.add(randomOption.getId());
                        }

                        answers.put(item.getId(), selectedIds);
                    }
                    // Enviamos las respuestas del bloque al servicio para que las procese
                    questionnaireService.submitBlockAnswers(updatedQ.getId(), block.getBlockNumber(), answers);
                }
            }
        }
        // --- FIN DE LA SIMULACIÓN DE CANDIDATOS ---

        // Vencemos la evaluación poniéndola en tiempo pasado
        evaluation.setCloseDate(LocalDateTime.now().minusDays(1));
        evaluationRepository.save(evaluation);

        // Forzamos la ejecución del Cron Job manualmente para que barra los vencidos
        questionnaireService.finalizarCuestionariosVencidos();

        return String.format(
                "¡Flujo completo simulado con éxito! \n" +
                        "Ruleta: %d Completos, %d Incompletos, %d Ignorados. \n" +
                        "La evaluación se cerró (fecha modificada al pasado) y el Cron Job barrió los cuestionarios pendientes.",
                countCompletos, countIncompletos, countIgnorados);
    }
}
