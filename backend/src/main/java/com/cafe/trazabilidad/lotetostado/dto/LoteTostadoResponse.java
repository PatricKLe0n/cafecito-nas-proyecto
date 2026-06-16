package com.cafe.trazabilidad.lotetostado.dto;

import com.cafe.trazabilidad.lotetostado.EstadoTostado;
import com.cafe.trazabilidad.lotetostado.PerfilTueste;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteTostadoResponse(
        Long id, String codigo, Long loteVerdeId, String loteVerdeCodigo,
        PerfilTueste perfilTueste, BigDecimal pesoEntradaKg, BigDecimal pesoSalidaKg,
        BigDecimal mermaPorcentaje, LocalDateTime fechaTueste, EstadoTostado estado) {}
