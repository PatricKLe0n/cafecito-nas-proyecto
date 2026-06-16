package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import org.mapstruct.*;

/**
 * Mapper MapStruct encargado de la conversión entre la entidad {@link Finca} y sus DTOs
 * de entrada ({@link FincaRequest}) y salida ({@link FincaResponse}).
 */
@Mapper(componentModel = "spring")
public interface FincaMapper {
    FincaResponse toResponse(Finca entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    Finca toEntity(FincaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void update(@MappingTarget Finca entity, FincaRequest request);
}
