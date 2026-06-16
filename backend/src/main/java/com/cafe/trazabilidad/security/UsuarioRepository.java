package com.cafe.trazabilidad.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Usuario}.
 * Expone las operaciones CRUD estándar de Spring Data JPA y la búsqueda
 * de usuarios por su nombre de acceso.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de acceso único.
     *
     * @param username nombre de acceso a localizar
     * @return el usuario envuelto en un {@link Optional}, o vacío si no existe
     */
    Optional<Usuario> findByUsername(String username);
}
