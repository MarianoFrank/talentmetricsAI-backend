package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.option.OptionDetailResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.option.OptionRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionDetailResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.question.QuestionSummaryResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Factor;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Option;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Question;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.FactorRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final String QUESTION_NOT_FOUND_MSG = "La pregunta no existe o fue eliminada";
    private final String FACTOR_NOT_FOUND_MSG = "El factor especificado no existe";
    private final QuestionRepository questionRepository;
    private final FactorRepository factorRepository;

    public QuestionService(QuestionRepository questionRepository, FactorRepository factorRepository) {
        this.questionRepository = questionRepository;
        this.factorRepository = factorRepository;
    }

    public Page<QuestionSummaryResponse> getAllQuestionsSummary(Long competencyId, Long factorId, String questionName,
            Pageable pageable) {
        return questionRepository.findAllSummaryQuestionsWithFilters(competencyId, factorId, questionName, pageable);
    }

    public QuestionDetailResponse getQuestionById(Long questionId) {
        Question q = questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_NOT_FOUND_MSG));

        List<OptionDetailResponse> optionsDTO = q.getOptions().stream()
                .map(opt -> new OptionDetailResponse(opt.getId(), opt.getDisplayOrder(), opt.getWeight(), opt.getText()))
                .toList();

        return new QuestionDetailResponse(
                q.getId(),
                q.getFactor().getId(),
                q.getName(),
                q.getText(),
                q.getDescription(),
                q.getType(),
                optionsDTO);
    }

    // --- MÉTODOS DE ESCRITURA (POST, PUT, DELETE) ---

    @Transactional
    public Question createQuestion(QuestionRequest dto) {
        Factor factor = factorRepository.findById(dto.factorId())
                .orElseThrow(() -> new IllegalArgumentException(FACTOR_NOT_FOUND_MSG));

        Question question = new Question();
        question.setFactor(factor);
        question.setName(dto.name());
        question.setText(dto.text());
        question.setDescription(dto.description());
        question.setType(dto.type());

        question.setVersion(1);

        for (OptionRequest optionDto : dto.options()) {
            Option option = new Option();
            option.setDisplayOrder(optionDto.displayOrder());
            option.setWeight(optionDto.weight());
            option.setText(optionDto.text());

            option.setQuestion(question);
            question.getOptions().add(option);
        }

        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Long questionId, QuestionRequest dto) {
        // ACÁ BLINDAMOS LA ACTUALIZACIÓN: Solo trae la pregunta si NO está borrada
        Question existingQuestion = questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_NOT_FOUND_MSG));

        existingQuestion.setName(dto.name());
        existingQuestion.setText(dto.text());
        existingQuestion.setDescription(dto.description());
        existingQuestion.setType(dto.type());

        if (!existingQuestion.getFactor().getId().equals(dto.factorId())) {
            Factor newFactor = factorRepository.findById(dto.factorId())
                    .orElseThrow(() -> new IllegalArgumentException(FACTOR_NOT_FOUND_MSG));
            existingQuestion.setFactor(newFactor);
        }

        existingQuestion.getOptions().clear();

        for (OptionRequest optionDto : dto.options()) {
            Option option = new Option();
            option.setDisplayOrder(optionDto.displayOrder());
            option.setWeight(optionDto.weight());
            option.setText(optionDto.text());

            option.setQuestion(existingQuestion);
            existingQuestion.getOptions().add(option);
        }

        return questionRepository.save(existingQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question existingQuestion = questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_NOT_FOUND_MSG));

        existingQuestion.setDeletedAt(LocalDateTime.now());

        questionRepository.save(existingQuestion);
    }
}
