package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa una finca de origen del café, es decir, el punto de partida
 * de la trazabilidad "de la finca a la taza".
 *
 * <p>Modela el lugar físico donde se cultiva y procesa el grano: su ubicación geográfica
 * (país, región, altitud), el productor responsable, la variedad cultivada y el proceso
 * de beneficio aplicado al grano.</p>
 *
 * <p>Invariantes clave:</p>
 * <ul>
 *   <li>El {@code nombre} es único dentro del sistema.</li>
 *   <li>Una finca no puede eliminarse mientras tenga lotes de café verde asociados.</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(name = "finca")
public class Finca extends BaseEntity {
    @Column(nullable = false) private String pais;
    @Column(nullable = false) private String region;
    @Column(nullable = false, unique = true) private String nombre;
    private String productor;
    @Column(name = "altitud_msnm") private Integer altitudMsnm;
    private String variedad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcesoBeneficio proceso;
}
