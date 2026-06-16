package com.cafe.trazabilidad.dashboard.dto;

import java.math.BigDecimal;

/** Resumen de indicadores generales del panel: lotes disponibles, registrados, merma media y total de fincas. */
public record ResumenResponse(
        long lotesVerdesDisponibles,
        long lotesTostadosRegistrados,
        BigDecimal mermaMediaPorcentaje,
        long totalFincas) {}
