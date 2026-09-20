package com.finanzassv.dto.reporte;

import java.math.BigDecimal;
import java.util.List;

/**
 * Balance General: Activo (1), Pasivo (2) y Capital Contable (3).
 * Demuestra la ecuacion contable: Activo = Pasivo + Capital Contable.
 * Incluye la Utilidad del periodo para la ecuacion extendida cuando hay
 * resultados pendientes de cierre.
 */
public record BalanceGeneralResponse(
        List<LineaReporte> activos,
        List<LineaReporte> pasivos,
        List<LineaReporte> capitalContable,
        BigDecimal totalActivo,
        BigDecimal totalPasivo,
        BigDecimal totalCapitalContable,
        BigDecimal utilidadPeriodo,
        BigDecimal pasivoMasCapital,
        /** true si Activo = Pasivo + Capital Contable (+ Utilidad). */
        Boolean ecuacionCumplida,
        BigDecimal diferencia) {
}
