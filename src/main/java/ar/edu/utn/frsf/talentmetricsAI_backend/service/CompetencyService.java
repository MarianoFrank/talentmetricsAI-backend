package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CompetencyRepository;

@Service
public class CompetencyService {

    private final CompetencyRepository competencyRepository;

    public CompetencyService(CompetencyRepository competencyRepository) {
        this.competencyRepository = competencyRepository;
    }

    public List<SelectItemResponse> getAllCompetenciesForSelect() {
        return competencyRepository.findAllForSelect();
    }
}
