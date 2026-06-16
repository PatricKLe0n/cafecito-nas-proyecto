package com.cafe.trazabilidad.dashboard.dto;

import java.math.BigDecimal;

public record ResumenResponse(
        long lotesVerdesDisponibles,
        long lotesTostadosRegistrados,
        BigDecimal mermaMediaPorcentaje,
        long totalFincas) {}
