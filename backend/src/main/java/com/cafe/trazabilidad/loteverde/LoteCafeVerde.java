package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.BaseEntity;
import com.cafe.trazabilidad.finca.Finca;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "lote_cafe_verde")
public class LoteCafeVerde extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finca_id")
    private Finca finca;

    @Column(name = "peso_kg", nullable = false)
    private BigDecimal pesoKg;

    @Column(name = "humedad_porcentaje")
    private BigDecimal humedadPorcentaje;

    @Column(name = "puntaje_sca")
    private BigDecimal puntajeSca;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLoteVerde estado;
}
