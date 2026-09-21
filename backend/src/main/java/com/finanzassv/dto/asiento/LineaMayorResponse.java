package com.finanzassv.dto.asiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimiento de una cuenta para el Libro Mayor. */
public record LineaMayorResponse(
        Long numeroAsiento,
        LocalDate fecha,
        String conceptoAsiento,
        String conceptoLinea,
        BigDecimal montoDebe,
        BigDecimal montoHaber) {
}
