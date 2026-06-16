package com.cafe.trazabilidad.common;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Payload uniforme de respuesta para los errores de la API.
 *
 * @param timestamp   instante en que se generó el error
 * @param status      código de estado HTTP
 * @param error       frase descriptiva del estado HTTP
 * @param message     mensaje legible que detalla la causa del error
 * @param path        ruta de la petición que provocó el error
 * @param fieldErrors errores de validación por campo (vacío si no aplica)
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    /** Detalle de un error de validación asociado a un campo concreto del payload. */
    public record FieldError(String field, String message) {}
}
