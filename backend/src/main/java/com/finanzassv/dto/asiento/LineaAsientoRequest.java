package com.finanzassv.dto.asiento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Linea Debe/Haber de un asiento (solo cuentas de movimiento).
 * El monto debe ir en EXACTAMENTE una de las dos columnas.
 */
public record LineaAsientoRequest(
        @NotNull(message = "El id de cuenta es obligatorio")
        Long cuentaId,

        @Size(max = 255, message = "El concepto no debe exceder 255 caracteres")
        String concepto,

        @PositiveOrZero(message = "El monto del Debe no puede ser negativo")
        BigDecimal montoDebe,

        @PositiveOrZero(message = "El monto del Haber no puede ser negativo")
        BigDecimal montoHaber) {
}
