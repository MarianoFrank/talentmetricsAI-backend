package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.company.CompanySelectDto;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('CONSULTANT')")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/select")
    public ResponseEntity<List<CompanySelectDto>> getCompaniesForSelect() {

        List<CompanySelectDto> companies = companyRepository.findAll().stream()
                .map(c -> new CompanySelectDto(c.getId(), c.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(companies);
    }
}
