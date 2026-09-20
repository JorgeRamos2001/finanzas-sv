package com.finanzassv.dto.asiento;

import com.finanzassv.enums.EstadoAsiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Version resumida para el listado del Libro Diario. */
public record AsientoResumenResponse(
        Long id,
        Long numeroAsiento,
        LocalDate fecha,
        String concepto,
        String usuarioNombre,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstadoAsiento estado) {
}
