package com.cafe.trazabilidad.common;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado con id " + id);
    }
}
