package com.cafe.trazabilidad.finca;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FincaRepository extends JpaRepository<Finca, Long> {
    Page<Finca> findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(
            String nombre, String region, Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
}
