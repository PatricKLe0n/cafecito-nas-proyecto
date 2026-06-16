package com.cafe.trazabilidad.loteverde.dto;

import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO de salida con la representación pública de un lote de café verde. */
public record LoteVerdeResponse(
        Long id, String codigo, Long fincaId, String fincaNombre,
        BigDecimal pesoKg, BigDecimal humedadPorcentaje, BigDecimal puntajeSca,
        LocalDate fechaRecepcion, EstadoLoteVerde estado) {}
