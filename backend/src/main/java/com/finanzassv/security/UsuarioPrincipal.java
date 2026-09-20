package com.finanzassv.security;

/**
 * Usuario autenticado extraido del token JWT.
 */
public record UsuarioPrincipal(
        Long id,
        String username,
        String rol,
        String nombreCompleto) {
}
