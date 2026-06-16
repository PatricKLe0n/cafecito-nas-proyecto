package com.cafe.trazabilidad.common;

/**
 * Excepción que señala la violación de una regla de negocio.
 *
 * <p>El manejador global la traduce a una respuesta HTTP 409 (Conflict).</p>
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
