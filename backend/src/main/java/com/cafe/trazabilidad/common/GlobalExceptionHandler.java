package com.cafe.trazabilidad.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.FieldError> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "Errores de validación", req, errores);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado", req, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req, List.of());
    }

    private ApiError.FieldError toFieldError(FieldError fe) {
        return new ApiError.FieldError(fe.getField(), fe.getDefaultMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String msg,
                                           HttpServletRequest req, List<ApiError.FieldError> fields) {
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(),
                status.getReasonPhrase(), msg, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
