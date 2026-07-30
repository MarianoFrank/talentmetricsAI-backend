package ar.edu.utn.frsf.talentmetricsAI_backend.security;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.auth.CandidateProfile;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.auth.ConsultantProfile;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateLoginRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Candidate;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionnaireState;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.questionnaire.Questionnaire;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.QuestionnaireRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.service.CandidateService;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("securityService") // Le ponemos nombre para llamarlo desde las anotaciones
public class SecurityService {

    private final QuestionnaireRepository questionnaireRepository;
    private final CandidateService candidateService;

    private final LdapService ldapService;
    private final JwtService jwtService;

    public SecurityService(QuestionnaireRepository questionnaireRepository, CandidateService candidateService,
            LdapService ldapService, JwtService jwtService) {
        this.questionnaireRepository = questionnaireRepository;
        this.candidateService = candidateService;
        this.ldapService = ldapService;
        this.jwtService = jwtService;
    }

    // principalName para el candidato es su documento, para el consultor es su
    // username de LDAP
    public Object getUserProfile(String principalName, String role) {
        String cleanRole = role.replace("ROLE_", "");

        if (cleanRole.equals("CANDIDATE")) {
            Candidate candidate = candidateService.findByDocumentNumber(principalName);

            return new CandidateProfile(principalName, cleanRole, candidate.getFirstName(), candidate.getLastName());
        } else {

            Map<String, Object> ldapData = ldapService.getUserProfile(principalName);
            if (ldapData == null)
                throw new RuntimeException("Usuario no encontrado en LDAP");

            String nombre = (String) ldapData.get("name");
            String apellido = (String) ldapData.get("lastname");
            String legajo = (String) ldapData.get("legajo");

            return new ConsultantProfile(principalName, cleanRole, nombre, apellido, legajo);
        }
    }

    // Validamos que el candidato que está haciendo la petición sea el dueño del
    // cuestionario
    @Transactional(readOnly = true)
    public boolean isCandidateOwner(Long questionnaireId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // obtenemos el nombre de usuario del candidato desde el token JWT, que seria su
        // documento
        String candidateUsernameFromToken = auth.getName();

        return questionnaireRepository.findById(questionnaireId)
                .map(q -> {
                    String actualDoc = q.getCandidate().getDocumentNumber();
                    return candidateUsernameFromToken.endsWith(actualDoc);
                })
                .orElse(false);
    }

    @Transactional
    public Map<String, Object> authenticateCandidate(CandidateLoginRequest request) {

        // Buscamos el cuestionario por la clave
        Questionnaire questionnaire = questionnaireRepository.findByAccessKey(request.getAccessCode())
                .orElseThrow(() -> new RuntimeException("Código de acceso inválido. Verifique e intente nuevamente."));

        if (questionnaire.getState() == QuestionnaireState.COMPLETED) {
            throw new RuntimeException("Este cuestionario ya fue completado.");
        }

        if (questionnaire.getState() == QuestionnaireState.INCOMPLETE ||
                questionnaire.getState() == QuestionnaireState.NOT_ANSWERED) {
            throw new RuntimeException("El plazo para completar este cuestionario ha finalizado.");
        }

        if (LocalDateTime.now().isAfter(questionnaire.getEvaluation().getCloseDate())) {
            throw new RuntimeException("La evaluación general para este puesto ya se encuentra cerrada.");
        }

        // Generamos un JWT para el candidato
        String candidateUsername = questionnaire.getCandidate().getDocumentNumber();
        String token = jwtService.generateToken(candidateUsername, "CANDIDATE");

        return Map.of(
                "token", token,
                "questionnaireId", questionnaire.getId());

    }
}
