package com.finanzassv.dto.cuenta;

import com.finanzassv.enums.NaturalezaCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CuentaRequest(
        @NotBlank
        @Size(max = 20)
        String codigo,

        @NotBlank
        @Size(max = 120)
        String nombre,

        /** Id de la cuenta padre (null para cuentas de nivel 1). */
        Long cuentaPadreId,

        @NotNull
        NaturalezaCuenta naturaleza,

        Boolean aceptaMovimientos,

        Boolean activo) {
}
