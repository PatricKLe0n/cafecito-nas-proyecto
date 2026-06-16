package com.cafe.trazabilidad.common;

/**
 * Excepción que indica que un recurso solicitado no existe.
 *
 * <p>El manejador global la traduce a una respuesta HTTP 404 (Not Found).</p>
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado con id " + id);
    }
}
