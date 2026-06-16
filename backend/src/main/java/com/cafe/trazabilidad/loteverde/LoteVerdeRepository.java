package com.cafe.trazabilidad.loteverde;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad {@link LoteCafeVerde}.
 * Expone las operaciones CRUD estándar de Spring Data JPA junto con consultas derivadas
 * para búsqueda paginada, filtrado por estado y verificación de unicidad del código.
 */
public interface LoteVerdeRepository extends JpaRepository<LoteCafeVerde, Long> {
    boolean existsByFincaId(Long fincaId);
    Page<LoteCafeVerde> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<LoteCafeVerde> findByEstado(EstadoLoteVerde estado, Pageable pageable);
    boolean existsByCodigoIgnoreCase(String codigo);
    long countByEstado(EstadoLoteVerde estado);
}
