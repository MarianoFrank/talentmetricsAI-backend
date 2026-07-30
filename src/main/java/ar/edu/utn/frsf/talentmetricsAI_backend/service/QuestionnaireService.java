package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.BlockResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.OptionItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.QuestionItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.questionnaire.StartQuestionnaireResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.event.QuestionnaireCompletedEvent;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final int QUESTIONS_PER_BLOCK = 3;

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository,
            QuestionRepository questionRepository, ApplicationEventPublisher eventPublisher) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public StartQuestionnaireResponse startQuestionnaire(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado"));

        int totalBlocks;
        int currentBlockNumber = 1;

        if (questionnaire.getState() == QuestionnaireState.IN_PROGRESS) {
            questionnaire.setLastAccess(LocalDateTime.now());
            questionnaire.setAccessCount(questionnaire.getAccessCount() + 1);
            questionnaireRepository.save(questionnaire);

            totalBlocks = questionnaire.getBlocks().size();
            currentBlockNumber = calcularBloqueActual(questionnaire);

            return new StartQuestionnaireResponse(
                    questionnaire.getId(),
                    totalBlocks,
                    currentBlockNumber,
                    questionnaire.getEvaluation().getDuration(),
                    questionnaire.getState().name(),
                    questionnaire.getStartedAt());
        }

        if (questionnaire.getState() != QuestionnaireState.ACTIVE) {
            throw new IllegalArgumentException("El cuestionario no se encuentra habilitado para ser completado");
        }

        // ----------------------------------------------------------------------
        // -- Si es la primera vez que entra GENERAMOS los bloques y preguntas --
        // ----------------------------------------------------------------------

        List<Question> selectedQuestions = new ArrayList<>();

        // Iteramos sobre las competencias del puesto y seleccionamos 2 preguntas
        // aleatorias de cada factor asociado a la competencia
        for (PositionCompetency pc : questionnaire.getEvaluation().getPosition().getCompetencies()) {
            Competency competency = pc.getCompetency();
            for (Factor factor : competency.getFactors()) {
                List<Question> factorQuestions = questionRepository.findActiveByFactorId(factor.getId());
                if (factorQuestions.size() >= 2) {
                    Collections.shuffle(factorQuestions);
                    selectedQuestions.add(factorQuestions.get(0));
                    selectedQuestions.add(factorQuestions.get(1));
                }
            }
        }

        // Mezclamos las preguntas seleccionadas
        Collections.shuffle(selectedQuestions);

        // Armado de bloques
        List<Block> blocks = new ArrayList<>();
        int blockNumber = 1;

        // creamos el primer bloque
        Block currentBlock = new Block();
        currentBlock.setQuestionnaire(questionnaire);
        currentBlock.setBlockNumber(blockNumber);
        currentBlock.setQuestionItems(new ArrayList<>());

        for (int i = 0; i < selectedQuestions.size(); i++) {

            // Cada vez que llegamos a QUESTIONS_PER_BLOCK, creamos un nuevo bloque
            if (i > 0 && i % QUESTIONS_PER_BLOCK == 0) {
                blocks.add(currentBlock);
                blockNumber++;

                currentBlock = new Block();
                currentBlock.setQuestionnaire(questionnaire);
                currentBlock.setBlockNumber(blockNumber);
                currentBlock.setQuestionItems(new ArrayList<>());
            }

            // Obtenemos la pregunta y sus opciones de la "fuente de datos"
            Question q = selectedQuestions.get(i);
            List<Option> opt = q.getOptions();

            // Creamos los items
            QuestionItem item = new QuestionItem();
            item.setBlock(currentBlock);
            item.setQuestion(q);
            item.setDisplayOrder((i % QUESTIONS_PER_BLOCK) + 1); // El orden de visualización dentro del bloque
            item.setOptionItems(new ArrayList<>());

            for (Option op : opt) {
                OptionItem optItem = new OptionItem();
                optItem.setQuestionItem(item);
                optItem.setOption(op);
                optItem.setIsAnswered(false);
                // Agregamos el OptionItem al QuestionItem
                item.getOptionItems().add(optItem);
            }

            // Agregamos el item al bloque
            currentBlock.getQuestionItems().add(item);
        }

        // Agregamos el último bloque si la cantidad de preguntas no es múltiplo de
        // QUESTIONS_PER_BLOCK (el ultimo bloque puede tener menos preguntas)
        if (!currentBlock.getQuestionItems().isEmpty()) {
            blocks.add(currentBlock);
        }

        // Guardamos los bloques en el cuestionario y actualizamos su estado
        questionnaire.getBlocks().addAll(blocks);
        questionnaire.setState(QuestionnaireState.IN_PROGRESS);
        questionnaire.setStartedAt(LocalDateTime.now());
        questionnaire.setLastAccess(LocalDateTime.now());
        questionnaire.setAccessCount(questionnaire.getAccessCount() + 1);

        questionnaireRepository.save(questionnaire);

        return new StartQuestionnaireResponse(
                questionnaire.getId(),
                blocks.size(),
                1,
                questionnaire.getEvaluation().getDuration(),
                questionnaire.getState().name(),
                questionnaire.getStartedAt());
    }

    @Transactional(readOnly = true)
    public BlockResponse getBlockByNumber(Long questionnaireId, int blockNumber) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado"));

        if (questionnaire.getState() != QuestionnaireState.IN_PROGRESS) {
            throw new IllegalArgumentException("El cuestionario no está en curso.");
        }

        int bloquePermitido = calcularBloqueActual(questionnaire);

        if (blockNumber != bloquePermitido) {
            throw new IllegalArgumentException(
                    "No tienes permiso para acceder a este bloque. Debes completar los bloques en orden.");
        }

        Block block = questionnaire.getBlocks().stream()
                .filter(b -> b.getBlockNumber() == blockNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado."));

        List<QuestionItemResponse> itemDTOs = new ArrayList<>();

        for (QuestionItem item : block.getQuestionItems()) {
            List<OptionItemResponse> optionDTOs = new ArrayList<>();

            for (OptionItem optItem : item.getOptionItems()) {
                optionDTOs.add(new OptionItemResponse(
                        optItem.getId(),
                        optItem.getOption().getDisplayOrder(),
                        optItem.getOption().getText(),
                        optItem.getIsAnswered()));
            }

            boolean isMultiple = item.getQuestion().getType() != null &&
                    item.getQuestion().getType().name().toUpperCase().contains("MULTIPLE");

            itemDTOs.add(new QuestionItemResponse(
                    item.getId(),
                    item.getDisplayOrder(),
                    item.getQuestion().getText(),
                    isMultiple,
                    optionDTOs));
        }

        return new BlockResponse(block.getId(), itemDTOs);
    }

    // Calcula el bloque actual que el candidato debería estar completando
    private int calcularBloqueActual(Questionnaire questionnaire) {
        for (Block block : questionnaire.getBlocks()) {
            // Los bloque viene ordenados desde la base de datos por blockNumber, así que el
            // primer bloque incompleto que encontremos es el actual
            boolean bloqueIncompleto = block.getQuestionItems().stream()
                    // Para cada item del bloque, verificamos si todas sus opciones están sin
                    // responder. Si alguna opción está respondida, el bloque no está incompleto
                    // (devolverá false).
                    .anyMatch(item -> item.getOptionItems().stream()
                            .noneMatch(optItem -> Boolean.TRUE.equals(optItem.getIsAnswered())));

            // Si el bloque está incompleto, devolvemos su número
            if (bloqueIncompleto) {
                return block.getBlockNumber();
            }
        }
        return questionnaire.getBlocks().size();
    }

    @Transactional
    public void submitBlockAnswers(Long questionnaireId, int blockNumber, Map<Long, List<Long>> answers) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado"));

        if (questionnaire.getState() != QuestionnaireState.IN_PROGRESS) {
            throw new IllegalArgumentException("El cuestionario ya fue finalizado o no está en curso.");
        }

        int bloquePermitido = calcularBloqueActual(questionnaire);
        if (blockNumber != bloquePermitido) {
            throw new IllegalArgumentException(
                    "No puedes enviar respuestas para este bloque. Debes completar los bloques en orden.");
        }

        Block block = questionnaire.getBlocks().stream()
                .filter(b -> b.getBlockNumber() == blockNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado."));

        for (QuestionItem item : block.getQuestionItems()) {
            // Obtenemos las opciones seleccionadas para esta pregunta
            List<Long> selectedOptionIds = answers.get(item.getId());

            if (selectedOptionIds == null) {
                throw new IllegalArgumentException("Falta responder preguntas del bloque.");
            }

            boolean isMultiple = item.getQuestion().getType().name().toUpperCase().contains("MULTIPLE");

            if (!isMultiple && selectedOptionIds.size() > 1) {
                throw new IllegalArgumentException(
                        "No podés seleccionar más de una opción en una pregunta de selección única.");
            }

            for (OptionItem optItem : item.getOptionItems()) {
                if (selectedOptionIds.contains(optItem.getId())) {
                    optItem.setIsAnswered(true);
                } else {
                    optItem.setIsAnswered(false);
                }
            }
        }

        // Si el bloque enviado es el último, marcamos el cuestionario como COMPLETED
        if (blockNumber == questionnaire.getBlocks().size()) {
            questionnaire.setState(QuestionnaireState.COMPLETED);
            questionnaire.setEndedAt(LocalDateTime.now());
            eventPublisher.publishEvent(new QuestionnaireCompletedEvent(questionnaire.getId()));
        }

        questionnaireRepository.save(questionnaire);
    }

    @Transactional
    public void finalizarCuestionariosVencidos() {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscamos los que están activos o en proceso
        List<QuestionnaireState> estadosAbiertos = List.of(
                QuestionnaireState.ACTIVE,
                QuestionnaireState.IN_PROGRESS);

        List<Questionnaire> cuestionariosVencidos = questionnaireRepository
                .findExpiredQuestionnaires(ahora, estadosAbiertos);

        if (cuestionariosVencidos.isEmpty()) {
            return;
        }

        for (Questionnaire q : cuestionariosVencidos) {
            if (q.getState() == QuestionnaireState.ACTIVE) {
                // Nunca entró
                q.setState(QuestionnaireState.NOT_ANSWERED);
            } else if (q.getState() == QuestionnaireState.IN_PROGRESS) {
                // Entró pero lo dejó por la mitad
                q.setState(QuestionnaireState.INCOMPLETE);
            }
            q.setEndedAt(ahora);
        }

        questionnaireRepository.saveAll(cuestionariosVencidos);

        System.out.println("Cuestionarios vencidos actualizados: " + cuestionariosVencidos.size());
    }
}
