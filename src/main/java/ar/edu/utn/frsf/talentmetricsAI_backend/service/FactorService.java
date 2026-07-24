package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import java.util.List;
import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.common.SelectItemResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.FactorRepository;

@Service
public class FactorService {

    private final FactorRepository factorRepository;

    public FactorService(FactorRepository factorRepository) {
        this.factorRepository = factorRepository;
    }

    public List<SelectItemResponse> getFactorsForSelect(Long competencyId) {
        if (competencyId != null) {
            return factorRepository.findForSelectByCompetencyId(competencyId);
        }
        return factorRepository.findAllForSelect();
    }
}
