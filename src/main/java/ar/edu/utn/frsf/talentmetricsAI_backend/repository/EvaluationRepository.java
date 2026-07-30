package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

}
