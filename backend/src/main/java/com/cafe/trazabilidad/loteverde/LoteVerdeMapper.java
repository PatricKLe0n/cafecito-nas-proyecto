package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteVerdeMapper {

    @Mapping(target = "fincaId", source = "finca.id")
    @Mapping(target = "fincaNombre", source = "finca.nombre")
    LoteVerdeResponse toResponse(LoteCafeVerde entity);
}
