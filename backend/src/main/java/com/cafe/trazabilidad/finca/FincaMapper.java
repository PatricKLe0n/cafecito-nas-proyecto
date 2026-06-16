package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import org.mapstruct.*;

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
