package com.cafe.trazabilidad.finca.dto;

import jakarta.validation.constraints.*;

public record FincaRequest(
        @NotBlank String pais,
        @NotBlank String region,
        @NotBlank String nombre,
        String productor,
        @Min(0) @Max(4000) Integer altitudMsnm,
        String variedad,
        @NotNull com.cafe.trazabilidad.finca.ProcesoBeneficio proceso) {}
