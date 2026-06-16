package com.cafe.trazabilidad.loteverde.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteVerdeRequest(
        @NotBlank String codigo,
        @NotNull Long fincaId,
        @NotNull @DecimalMin("0.0") BigDecimal pesoKg,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal humedadPorcentaje,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal puntajeSca,
        @NotNull @PastOrPresent LocalDate fechaRecepcion) {}
