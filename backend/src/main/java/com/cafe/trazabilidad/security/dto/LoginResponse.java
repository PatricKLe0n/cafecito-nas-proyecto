package com.cafe.trazabilidad.security.dto;

/** Payload de respuesta del login con el token JWT emitido y los datos del usuario autenticado. */
public record LoginResponse(String token, String username, String rol, long expiraEnMs) {}
