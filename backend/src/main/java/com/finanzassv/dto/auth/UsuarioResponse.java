package com.finanzassv.dto.auth;

import com.finanzassv.enums.RolUsuario;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        RolUsuario rol,
        Boolean activo) {
}
