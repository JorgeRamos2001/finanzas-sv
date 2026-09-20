package com.finanzassv.dto.asiento;

import com.finanzassv.enums.EstadoAsiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public record AsientoResponse(
        Long id,
        Long numeroAsiento,
        LocalDate fecha,
        String concepto,
        Long usuarioId,
        String usuarioNombre,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstadoAsiento estado,
        Instant fechaCreacion,
        List<LineaAsientoResponse> detalles) {
}
