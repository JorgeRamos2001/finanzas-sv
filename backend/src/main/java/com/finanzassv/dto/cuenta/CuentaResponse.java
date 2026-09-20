package com.finanzassv.dto.cuenta;

import com.finanzassv.enums.NaturalezaCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String codigo,
        String nombre,
        Integer nivel,
        Long cuentaPadreId,
        String cuentaPadreNombre,
        NaturalezaCuenta naturaleza,
        Boolean aceptaMovimientos,
        BigDecimal saldoDebe,
        BigDecimal saldoHaber,
        BigDecimal saldoNeto,
        /** Naturaleza del SALDO actual: DEUDOR si saldoDebe > saldoHaber. */
        String tipoSaldo,
        Boolean activo) {
}
