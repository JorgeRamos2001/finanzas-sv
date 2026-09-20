package com.finanzassv.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del servicio JWT: generacion, validacion y rechazo de tokens.
 */
class JwtServiceTest {

    private static final String SECRETO =
            "clave-de-prueba-suficientemente-larga-para-hmac-sha-256";

    private final JwtService jwtService = new JwtService(SECRETO, 3_600_000L);

    @Test
    @DisplayName("Genera y valida un token con los claims del usuario")
    void generaYValidaToken() {
        var usuario = new UsuarioPrincipal(2L, "contador", "CONTADOR", "Contador General");

        String token = jwtService.generarToken(usuario);
        var validado = jwtService.validarToken(token);

        assertThat(validado).isNotNull();
        assertThat(validado.id()).isEqualTo(2L);
        assertThat(validado.username()).isEqualTo("contador");
        assertThat(validado.rol()).isEqualTo("CONTADOR");
        assertThat(validado.nombreCompleto()).isEqualTo("Contador General");
    }

    @Test
    @DisplayName("RECHAZA un token alterado (firma invalida)")
    void rechazaTokenAlterado() {
        var token = jwtService.generarToken(
                new UsuarioPrincipal(1L, "admin", "ADMIN", "Administrador"));
        String alterado = token.substring(0, token.length() - 3) + "xxx";

        assertThat(jwtService.validarToken(alterado)).isNull();
    }

    @Test
    @DisplayName("RECHAZA un token expirado")
    void rechazaTokenExpirado() {
        var expirado = new JwtService(SECRETO, -1_000L);
        String token = expirado.generarToken(
                new UsuarioPrincipal(1L, "admin", "ADMIN", "Administrador"));

        assertThat(jwtService.validarToken(token)).isNull();
    }

    @Test
    @DisplayName("RECHAZA un secreto menor a 32 caracteres (256 bits)")
    void rechazaSecretoCorto() {
        assertThatThrownBy(() -> new JwtService("secreto-corto", 3_600_000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 caracteres");
    }
}
