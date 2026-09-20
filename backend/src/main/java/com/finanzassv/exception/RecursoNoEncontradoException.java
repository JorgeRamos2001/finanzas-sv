package com.finanzassv.exception;

/**
 * Excepcion de recurso no encontrado: se traduce en HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
