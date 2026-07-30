package ar.edu.utn.frsf.talentmetricsAI_backend.service;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.competency.CompetencyCountDTO;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.competency.PositionSelectDTO;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Position;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.PositionRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CompetencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final CompetencyRepository competencyRepository;

    public PositionService(PositionRepository positionRepository, CompetencyRepository competencyRepository) {
        this.positionRepository = positionRepository;
        this.competencyRepository = competencyRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionSelectDTO> getPositionsForSelect() {
        // Buscamos todos los puestos activos
        List<Position> activePositions = positionRepository.findAllActivePositions();

        // Buscamos los IDs de las competencias que CUMPLEN la condición
        Set<Long> validCompetencyIds = new HashSet<>(competencyRepository.findValidCompetencyIds());

        return activePositions.stream().map(position -> {

            List<CompetencyCountDTO> competencies = position.getCompetencies().stream()
                    .map(cp -> {
                        Long compId = cp.getCompetency().getId();
                        // Verificamos si la competencia está en el Set de competencias válidas
                        boolean isValid = validCompetencyIds.contains(compId);

                        return new CompetencyCountDTO(
                                cp.getCompetency().getName(),
                                cp.getWeightingRequired(),
                                isValid);
                    })
                    .collect(Collectors.toList());

            return new PositionSelectDTO(
                    position.getId(),
                    position.getName(),
                    position.getCompany().getName(),
                    competencies);

        }).collect(Collectors.toList());
    }
}
