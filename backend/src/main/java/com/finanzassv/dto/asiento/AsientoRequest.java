package com.finanzassv.dto.asiento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Asiento del Libro Diario con sus lineas.
 *
 * Regla critica: la suma de Debe debe ser EXACTAMENTE igual a la suma del Haber.
 * El backend rechaza el asiento con HTTP 400 si no se cumple la partida doble.
 */
public record AsientoRequest(
        @NotNull(message = "La fecha es obligatoria")
        @PastOrPresent(message = "La fecha no puede ser futura")
        LocalDate fecha,

        @NotBlank(message = "El concepto general es obligatorio")
        @Size(max = 255)
        String concepto,

        @NotEmpty(message = "El asiento debe tener al menos dos lineas")
        @Valid
        List<LineaAsientoRequest> lineas) {
}
