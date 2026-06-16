package com.cafe.trazabilidad.security.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload de la petición de inicio de sesión con las credenciales del usuario. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {}
