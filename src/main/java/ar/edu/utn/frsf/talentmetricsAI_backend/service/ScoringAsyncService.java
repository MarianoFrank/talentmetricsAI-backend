package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.event.QuestionnaireCompletedEvent;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;

import java.util.ArrayList;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class ScoringAsyncService {

    private final QuestionnaireRepository questionnaireRepository;

    public ScoringAsyncService(QuestionnaireRepository questionnaireRepository) {
        this.questionnaireRepository = questionnaireRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // Espera que el Hilo Principal (que lanza el evento) haya hecho el COMMIT
    // exitoso en la base de datos.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void calcularYGuardarPuntajesAsync(QuestionnaireCompletedEvent event) {

        Long questionnaireId = event.questionnaireId();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado para cálculo"));

        // Aseguramos que la lista no sea nula antes de limpiarla
        if (questionnaire.getCompetencyScores() != null) {
            questionnaire.getCompetencyScores().clear();
        } else {
            questionnaire.setCompetencyScores(new ArrayList<>());
        }

        int cantidadCompetencias = questionnaire.getEvaluation().getPosition().getCompetencies().size();
        double sumaTotalesCompetencias = 0.0;

        for (PositionCompetency pc : questionnaire.getEvaluation().getPosition().getCompetencies()) {
            Competency competency = pc.getCompetency();

            CompetencyScore compScore = new CompetencyScore();
            compScore.setQuestionnaire(questionnaire);
            compScore.setCompetency(competency);
            compScore.setFactorScores(new ArrayList<>());

            double sumaTotalesFactores = 0.0;
            int cantidadFactoresDeEstaCompetencia = competency.getFactors().size();

            for (Factor factor : competency.getFactors()) {
                boolean factorFueEvaluado = false;
                double sumaPesosFactor = 0;

                for (Block block : questionnaire.getBlocks()) {
                    for (QuestionItem item : block.getQuestionItems()) {
                        if (item.getQuestion().getFactor().getId().equals(factor.getId())) {
                            factorFueEvaluado = true;

                            double sumaPesosPregunta = 0.0;
                            double penalizacion = 0.0;

                            boolean isMultiple = item.getQuestion().getType() != null &&
                                    item.getQuestion().getType().name().toUpperCase().contains("MULTIPLE");

                            // Valor que se restará por cada opción "basura" seleccionada
                            double valorPenalizacion = 0.0;
                            if (!item.getOptionItems().isEmpty()) {
                                valorPenalizacion = 10.0 / item.getOptionItems().size();
                            }

                            for (OptionItem optItem : item.getOptionItems()) {
                                if (Boolean.TRUE.equals(optItem.getIsAnswered())) {
                                    double peso = optItem.getOption().getWeight();
                                    sumaPesosPregunta += peso;

                                    /*
                                     * NOTA DE DISEÑO
                                     * Como el documento especifica que los pesos deben sumar 10 y ser positivos,
                                     * existe la vulnerabilidad de que en un "Multiple Choice" el candidato
                                     * seleccione absolutamente todas las opciones y obtenga puntaje perfecto (10).
                                     *
                                     * Para solucionarlo sin romper las preguntas "blandas" (donde todas las
                                     * opciones pueden tener un peso válido, ej: 4, 3, 2, 1), aplicamos una
                                     * penalización
                                     * SOLO si el candidato selecciona opciones que valen 0.0 (distractores).
                                     */
                                    if (isMultiple && peso == 0.0) {
                                        penalizacion += valorPenalizacion;
                                    }
                                }
                            }

                            // Aplicamos la penalización asegurando que el puntaje no baje de 0
                            double puntajeFinalPregunta = Math.max(0.0, sumaPesosPregunta - penalizacion);
                            sumaPesosFactor += puntajeFinalPregunta;
                        }
                    }
                }

                if (factorFueEvaluado) {
                    // el puntaje del factor es la suma de los pesos de sus preguntas dividido por 2
                    double puntajeFactor = sumaPesosFactor / 2.0;

                    FactorScore fScore = new FactorScore();
                    fScore.setCompetencyScore(compScore);
                    fScore.setFactor(factor);
                    fScore.setScore(puntajeFactor);

                    compScore.getFactorScores().add(fScore);
                    sumaTotalesFactores += puntajeFactor;
                }
            }

            double puntajeCompetencia = 0.0;
            if (cantidadFactoresDeEstaCompetencia > 0) {
                puntajeCompetencia = sumaTotalesFactores / cantidadFactoresDeEstaCompetencia;
            }

            compScore.setScore(puntajeCompetencia);
            questionnaire.getCompetencyScores().add(compScore);
            sumaTotalesCompetencias += puntajeCompetencia;
        }

        double puntajeTotalCuestionario = 0.0;
        if (cantidadCompetencias > 0) {
            puntajeTotalCuestionario = sumaTotalesCompetencias / cantidadCompetencias;
        }

        questionnaire.setTotalScore(puntajeTotalCuestionario);

        questionnaireRepository.save(questionnaire);
    }
}
