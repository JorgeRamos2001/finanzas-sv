package com.finanzassv.enums;

/**
 * Estados posibles de un asiento del Libro Diario.
 */
public enum EstadoAsiento {
    /** Asiento registrado y mayorizado. */
    REGISTRADO,
    /** Asiento anulado: no afecta los saldos del Libro Mayor. */
    ANULADO
}
