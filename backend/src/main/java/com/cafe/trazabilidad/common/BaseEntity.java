package com.cafe.trazabilidad.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Superclase base para las entidades de persistencia del dominio.
 *
 * <p>Aporta el identificador autogenerado y la marca de tiempo de creación, que se asigna
 * automáticamente la primera vez que la entidad se persiste y permanece inmutable. Las
 * entidades concretas heredan estos atributos comunes para evitar duplicarlos.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
