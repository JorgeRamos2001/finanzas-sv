package com.finanzassv.dto.auth;

public record TokenResponse(
        String token,
        String tipoToken,
        long expiracionMs,
        UsuarioResponse usuario) {
}
