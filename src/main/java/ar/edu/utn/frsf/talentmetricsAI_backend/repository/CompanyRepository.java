package ar.edu.utn.frsf.talentmetricsAI_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frsf.talentmetricsAI_backend.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}
