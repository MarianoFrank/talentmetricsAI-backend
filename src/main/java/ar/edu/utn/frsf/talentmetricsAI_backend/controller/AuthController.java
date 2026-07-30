package ar.edu.utn.frsf.talentmetricsAI_backend.controller;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateLoginRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.dto.candidate.CandidateLoginResponse;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.Role;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.User;
import ar.edu.utn.frsf.talentmetricsAI_backend.repository.UserRepository;
import ar.edu.utn.frsf.talentmetricsAI_backend.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ar.edu.utn.frsf.talentmetricsAI_backend.security.LdapService;
import ar.edu.utn.frsf.talentmetricsAI_backend.security.SecurityService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LdapService ldapService;
    private final SecurityService securityService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
            UserRepository userRepository, LdapService ldapService,
            SecurityService securityService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.ldapService = ldapService;
        this.securityService = securityService;

    }

    // Sumamos el tipoAcceso que pide el diseño del TP
    public record LoginRequest(String username, String password) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Validamos credenciales contra LDAP
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password()));

        // JIT PROVISIONING: Buscamos en la BD local. Si no existe, lo creamos
        User user = userRepository.findByUsername(loginRequest.username()).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(loginRequest.username());
            newUser.setRole(Role.CONSULTANT);
            return userRepository.save(newUser);
        });

        // Generamos access y refresh token
        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        jwtService.generateJwtCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE,
                        jwtService.generateRefreshJwtCookie(refreshToken).toString())
                .body(Map.of("message", "Consultor autenticado correctamente", "user",
                        Map.of("username", user.getUsername(), "role", user.getRole().name())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = getCookieValue(request, "jwt_talentmetrics_refresh");

        if (refreshToken == null || !jwtService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token inválido o expirado"));
        }

        String username = jwtService.extractUsername(refreshToken);

        if (!ldapService.isUserActive(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error",
                    "El usuario ya no existe o fue dado de baja en el directorio corporativo"));
        }

        String role = jwtService.extractRole(refreshToken).replace("ROLE_", "");
        String newAccessToken = jwtService.generateToken(username, role);
        String newRefreshToken = jwtService.generateRefreshToken(username, role);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        jwtService.generateJwtCookie(newAccessToken).toString())
                .header(HttpHeaders.SET_COOKIE,
                        jwtService.generateRefreshJwtCookie(newRefreshToken).toString())
                .body(Map.of("message", "Token renovado correctamente", "user",
                        Map.of("username", username, "role", role)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        String cleanCookie = jwtService.getCleanJwtCookie().toString();
        String cleanRefreshCookie = jwtService.getCleanRefreshJwtCookie().toString();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleanCookie)
                .header(HttpHeaders.SET_COOKIE, cleanRefreshCookie)
                .body(Map.of("message", "Sesión cerrada"));
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(Authentication authentication) {
        String principalName = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Object profile = securityService.getUserProfile(principalName, role);

        return ResponseEntity.ok(profile);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @PostMapping("/candidate/login")
    public ResponseEntity<CandidateLoginResponse> loginCandidate(@RequestBody CandidateLoginRequest request) {
        try {

            Map<String, Object> response = securityService.authenticateCandidate(request);

            Long questionnaireId = (Long) response.get("questionnaireId");
            String token = (String) response.get("token");

            CandidateLoginResponse candidateLoginResponse = new CandidateLoginResponse(questionnaireId);
            // Reutilizamos tu JwtService para generar la cookie con el token del candidato
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtService.generateJwtCookie(token).toString())
                    .body(candidateLoginResponse);

        } catch (RuntimeException e) {
            // Mandamos un 401 Unauthorized si la clave está mal o el cuestionario venció
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
