package com.cafe.trazabilidad.dashboard.dto;

import java.math.BigDecimal;

/** Stock total de café verde (kg) agrupado por finca de origen. */
public record StockPorFinca(String finca, BigDecimal stockKg) {}
