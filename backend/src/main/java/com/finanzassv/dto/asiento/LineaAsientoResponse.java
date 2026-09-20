package com.finanzassv.dto.asiento;

import java.math.BigDecimal;

public record LineaAsientoResponse(
        Long id,
        Long cuentaId,
        String cuentaCodigo,
        String cuentaNombre,
        String concepto,
        BigDecimal montoDebe,
        BigDecimal montoHaber,
        Integer ordenLinea) {
}
