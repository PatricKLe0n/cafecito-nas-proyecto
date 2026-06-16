package com.cafe.trazabilidad.loteverde;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteVerdeRepository extends JpaRepository<LoteCafeVerde, Long> {
    boolean existsByFincaId(Long fincaId);
    Page<LoteCafeVerde> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<LoteCafeVerde> findByEstado(EstadoLoteVerde estado, Pageable pageable);
    boolean existsByCodigoIgnoreCase(String codigo);
    long countByEstado(EstadoLoteVerde estado);
}
