package com.finanzassv.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Cuerpo estandar de respuesta para todos los errores de la API.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        Map<String, String> campos) {

    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(Instant.now(), status, error, mensaje, null);
    }

    public static ErrorResponse of(int status, String error, String mensaje, Map<String, String> campos) {
        return new ErrorResponse(Instant.now(), status, error, mensaje, campos);
    }
}
