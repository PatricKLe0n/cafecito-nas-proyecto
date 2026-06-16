package com.cafe.trazabilidad.lotetostado.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteTostadoRequest(
        @NotBlank String codigo,
        @NotNull Long loteVerdeId,
        @NotNull com.cafe.trazabilidad.lotetostado.PerfilTueste perfilTueste,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pesoEntradaKg,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pesoSalidaKg,
        @NotNull @PastOrPresent LocalDateTime fechaTueste) {}
