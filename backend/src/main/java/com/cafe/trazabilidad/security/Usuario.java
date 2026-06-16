package com.cafe.trazabilidad.security;

import com.cafe.trazabilidad.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de persistencia que representa una cuenta de usuario del sistema.
 * Almacena las credenciales de acceso (con la contraseña codificada mediante BCrypt)
 * y el rol que determina los permisos del usuario dentro de la aplicación.
 */
@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
}
