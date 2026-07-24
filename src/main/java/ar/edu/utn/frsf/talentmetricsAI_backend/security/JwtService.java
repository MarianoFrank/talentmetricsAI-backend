package ar.edu.utn.frsf.talentmetricsAI_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// Servicio que maneja todo lo que tenga que ver con manejar JWT: generar, validar, extraer datos, etc.
@Service
public class JwtService {

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
    private static final String ACCESS_COOKIE_NAME = "jwt_talentmetrics";
    private static final String REFRESH_COOKIE_NAME = "jwt_talentmetrics_refresh";

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION_TIME;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String username, String roleName) {
        return generateToken(username, roleName, ACCESS_TOKEN_TYPE, EXPIRATION_TIME);
    }

    public String generateRefreshToken(String username, String roleName) {
        return generateToken(username, roleName, REFRESH_TOKEN_TYPE, REFRESH_EXPIRATION_TIME);
    }

    private String generateToken(String username, String roleName, String tokenType, long expirationTime) {
        return Jwts.builder()
                .claim("type", tokenType)
                .claim("role", "ROLE_" + roleName) // Guardamos el rol con el prefijo que usa Spring
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token) && REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
    }

    public ResponseCookie generateJwtCookie(String jwt) {
        return ResponseCookie.from(ACCESS_COOKIE_NAME, jwt)
                .httpOnly(true)
                .secure(false) // Pasar a true si usás HTTPS en producción
                .path("/")
                .maxAge(EXPIRATION_TIME / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie generateRefreshJwtCookie(String jwt) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, jwt)
                .httpOnly(true)
                .secure(false)
                // hace que la cookie solo la envíe el navegador al endpoint de refresh, no a
                // otros endpoints
                .path("/api/auth/refresh")
                .maxAge(REFRESH_EXPIRATION_TIME / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return cleanCookie(ACCESS_COOKIE_NAME);
    }

    public ResponseCookie getCleanRefreshJwtCookie() {
        return cleanCookie(REFRESH_COOKIE_NAME);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private ResponseCookie cleanCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "").path("/").maxAge(0).build();
    }
}
