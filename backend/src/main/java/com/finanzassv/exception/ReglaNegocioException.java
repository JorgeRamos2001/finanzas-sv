package com.finanzassv.exception;

/**
 * Excepcion de regla de negocio: se traduce en HTTP 400.
 * Ejemplo: violacion de la Ley de la Partida Doble.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
