package com.cafe.trazabilidad.finca.dto;

import jakarta.validation.constraints.*;

/** DTO de entrada con los datos necesarios para crear o actualizar una finca. */
public record FincaRequest(
        @NotBlank String pais,
        @NotBlank String region,
        @NotBlank String nombre,
        String productor,
        @Min(0) @Max(4000) Integer altitudMsnm,
        String variedad,
        @NotNull com.cafe.trazabilidad.finca.ProcesoBeneficio proceso) {}
