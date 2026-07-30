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
// ⚠️ ACORDATE DE BORRAR O COMENTAR ESTA CLASE ANTES DE LA DEFENSA DEL TP ⚠️
public class MockDataController {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireService questionnaireService;
    private final EvaluationRepository evaluationRepository; // 👈 AGREGAMOS ESTO

    public MockDataController(QuestionnaireRepository questionnaireRepository,
            QuestionnaireService questionnaireService,
            EvaluationRepository evaluationRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionnaireService = questionnaireService;
        this.evaluationRepository = evaluationRepository;
    }

    @PostMapping("/answer-all/{evaluationId}")
    @Transactional // 🛡️ EL ESCUDO ANTI-LAZY EXCEPTION
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

                // CASO 1: 20% ni entran
                if (decision < 20) {
                    countIgnorados++;
                    continue;
                }

                questionnaireService.startQuestionnaire(q.getId());
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
                    questionnaireService.submitBlockAnswers(updatedQ.getId(), block.getBlockNumber(), answers);
                }
            }
        }
        // --- FIN DE LA SIMULACIÓN DE CANDIDATOS ---

        // ⚠️ ACÁ VIENE LA MAGIA DEL CIERRE Y EL CRON JOB ⚠️

        // 2. Viajamos en el tiempo: vencemos la evaluación poniéndola en el pasado
        evaluation.setCloseDate(LocalDateTime.now().minusDays(1));
        evaluationRepository.save(evaluation);

        // 3. Forzamos la ejecución del Cron Job manualmente para que barra los vencidos
        questionnaireService.finalizarCuestionariosVencidos();

        return String.format(
                "¡Flujo completo simulado con éxito! \n" +
                        "Ruleta: %d Completos, %d Incompletos, %d Ignorados. \n" +
                        "La evaluación se cerró (fecha modificada al pasado) y el Cron Job barrió los cuestionarios pendientes.",
                countCompletos, countIncompletos, countIgnorados);
    }
}
