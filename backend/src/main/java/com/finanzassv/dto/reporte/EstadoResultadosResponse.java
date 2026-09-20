package com.finanzassv.dto.reporte;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estado de Resultados: Ingresos (5) y Costos y Gastos (4).
 * Calcula automaticamente: Ingresos - Costos y Gastos = Utilidad.
 */
public record EstadoResultadosResponse(
        List<LineaReporte> ingresos,
        List<LineaReporte> costosYGastos,
        BigDecimal totalIngresos,
        BigDecimal totalCostosYGastos,
        BigDecimal utilidad,
        /** PERDIDA si la utilidad es negativa. */
        String resultado) {
}
