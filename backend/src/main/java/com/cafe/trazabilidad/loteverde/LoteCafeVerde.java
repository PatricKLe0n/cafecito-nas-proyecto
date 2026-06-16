package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.BaseEntity;
import com.cafe.trazabilidad.finca.Finca;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa un lote de café verde (grano crudo) recibido desde una finca de origen.
 *
 * <p>Modela una recepción concreta de café sin tostar, identificada por un código único y
 * caracterizada por su finca de procedencia, fecha de recepción y atributos de calidad
 * (humedad y puntaje SCA).</p>
 *
 * <p>Invariantes clave:</p>
 * <ul>
 *   <li>El {@code codigo} es único dentro del sistema.</li>
 *   <li>El {@code pesoKg} representa el stock de café verde disponible.</li>
 *   <li>El {@code estado} refleja la disponibilidad del lote: {@code DISPONIBLE} o {@code AGOTADO}.</li>
 *   <li>Un lote no puede eliminarse mientras tenga lotes tostados asociados.</li>
 * </ul>
 */
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
