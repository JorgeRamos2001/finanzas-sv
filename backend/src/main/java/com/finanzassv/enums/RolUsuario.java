package com.finanzassv.enums;

/**
 * Roles de acceso del sistema.
 */
public enum RolUsuario {
    /** Acceso total: gestiona usuarios y catalogo de cuentas. */
    ADMIN,
    /** Registra asientos y consulta reportes financieros. */
    CONTADOR,
    /** Solo consulta reportes financieros. */
    CONSULTA
}
