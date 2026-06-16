package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
