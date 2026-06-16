package com.cafe.trazabilidad.finca.dto;

import com.cafe.trazabilidad.finca.ProcesoBeneficio;

public record FincaResponse(
        Long id, String pais, String region, String nombre, String productor,
        Integer altitudMsnm, String variedad, ProcesoBeneficio proceso) {}
