package com.cafe.trazabilidad.finca.dto;

import com.cafe.trazabilidad.finca.ProcesoBeneficio;

/** DTO de salida con la representación pública de una finca. */
public record FincaResponse(
        Long id, String pais, String region, String nombre, String productor,
        Integer altitudMsnm, String variedad, ProcesoBeneficio proceso) {}
