package com.finanzassv.dto.reporte;

import com.finanzassv.enums.NaturalezaCuenta;

import java.math.BigDecimal;

/** Linea de un estado financiero (agrupada por el primer digito del codigo). */
public record LineaReporte(
        String codigo,
        String nombre,
        NaturalezaCuenta naturaleza,
        BigDecimal saldoDebe,
        BigDecimal saldoHaber,
        BigDecimal saldoNeto,
        /** Naturaleza del saldo actual: DEUDOR o ACREEDOR. */
        String tipoSaldo) {
}
