package com.finanzassv.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Generacion y validacion de tokens JWT (HMAC-SHA256).
 */
@Service
public class JwtService {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";
    public static final String CLAIM_UID = "uid";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration-ms}") long expiracionMs) {
        if (secreto == null || secreto.getBytes().length < 32) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos 32 caracteres (256 bits) para HMAC-SHA256");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes());
        this.expiracionMs = expiracionMs;
    }

    /** Genera un token firmado para el usuario indicado. */
    public String generarToken(UsuarioPrincipal usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.username())
                .claim(CLAIM_UID, usuario.id())
                .claim(CLAIM_ROL, usuario.rol())
                .claim(CLAIM_NOMBRE, usuario.nombreCompleto())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMs)))
                .signWith(clave)
                .compact();
    }

    /** Vigencia configurada del token en milisegundos. */
    public long expiracionMs() {
        return expiracionMs;
    }

    /**
     * Valida la firma y vigencia del token y devuelve el UsuarioPrincipal.
     * Devuelve null si el token es invalido o esta expirado.
     */
    public UsuarioPrincipal validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new UsuarioPrincipal(
                    claims.get(CLAIM_UID, Long.class),
                    claims.getSubject(),
                    claims.get(CLAIM_ROL, String.class),
                    claims.get(CLAIM_NOMBRE, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
