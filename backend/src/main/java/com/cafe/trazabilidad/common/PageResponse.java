package com.cafe.trazabilidad.common;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Representación serializable de una página de resultados expuesta por la API.
 *
 * <p>Encapsula el contenido junto con los metadatos de paginación (número y tamaño de
 * página, totales e indicadores de primera y última página), desacoplando la respuesta del
 * tipo {@link Page} de Spring Data.</p>
 *
 * @param <T> tipo de los elementos contenidos en la página
 */
public record PageResponse<T>(
        List<T> content, int page, int size,
        long totalElements, int totalPages, boolean first, boolean last
) {
    /**
     * Construye un {@code PageResponse} a partir de una página de Spring Data.
     *
     * @param p   página de origen
     * @param <T> tipo de los elementos contenidos en la página
     * @return la página adaptada al formato de respuesta de la API
     */
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }
}
