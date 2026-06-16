package com.cafe.trazabilidad.loteverde.dto;

import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteVerdeResponse(
        Long id, String codigo, Long fincaId, String fincaNombre,
        BigDecimal pesoKg, BigDecimal humedadPorcentaje, BigDecimal puntajeSca,
        LocalDate fechaRecepcion, EstadoLoteVerde estado) {}
