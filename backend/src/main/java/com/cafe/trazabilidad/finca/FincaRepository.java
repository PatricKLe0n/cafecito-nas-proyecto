package com.cafe.trazabilidad.finca;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad {@link Finca}.
 * Expone las operaciones CRUD estándar de Spring Data JPA junto con consultas
 * derivadas para búsqueda paginada y verificación de unicidad del nombre.
 */
public interface FincaRepository extends JpaRepository<Finca, Long> {
    Page<Finca> findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(
            String nombre, String region, Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
}
