package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.BaseEntity;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una tanda de tostado obtenida a partir de un
 * {@link LoteCafeVerde}. Registra los pesos de entrada y salida, la merma
 * resultante del proceso, el perfil de tueste aplicado y el estado de la tanda.
 * <p>
 * La merma (porcentaje) se calcula en el servidor a partir de los pesos de
 * entrada y salida; el estado controla si la tanda está vigente o anulada.
 */
@Getter
@Setter
@Entity
@Table(name = "lote_tostado")
public class LoteTostado extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_verde_id")
    private LoteCafeVerde loteVerde;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_tueste", nullable = false)
    private PerfilTueste perfilTueste;

    @Column(name = "peso_entrada_kg", nullable = false)
    private BigDecimal pesoEntradaKg;

    @Column(name = "peso_salida_kg", nullable = false)
    private BigDecimal pesoSalidaKg;

    @Column(name = "merma_porcentaje", nullable = false)
    private BigDecimal mermaPorcentaje;

    @Column(name = "fecha_tueste", nullable = false)
    private LocalDateTime fechaTueste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTostado estado;
}
