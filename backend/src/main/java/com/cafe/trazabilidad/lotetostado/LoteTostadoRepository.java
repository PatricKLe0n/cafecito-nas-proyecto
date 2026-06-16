package com.cafe.trazabilidad.lotetostado;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos de {@link LoteTostado}. Ofrece la persistencia
 * estándar de Spring Data junto con consultas derivadas de búsqueda, filtrado y
 * conteo por código, perfil de tueste y estado.
 */
public interface LoteTostadoRepository extends JpaRepository<LoteTostado, Long> {
    boolean existsByLoteVerdeId(Long loteVerdeId);
    Page<LoteTostado> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<LoteTostado> findByPerfilTueste(PerfilTueste perfil, Pageable pageable);
    Page<LoteTostado> findByEstado(EstadoTostado estado, Pageable pageable);
    long countByEstado(EstadoTostado estado);
    long countByPerfilTuesteAndEstado(PerfilTueste perfil, EstadoTostado estado);
}
