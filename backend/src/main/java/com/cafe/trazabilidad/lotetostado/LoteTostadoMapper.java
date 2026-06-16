package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteTostadoMapper {

    @Mapping(target = "loteVerdeId", source = "loteVerde.id")
    @Mapping(target = "loteVerdeCodigo", source = "loteVerde.codigo")
    LoteTostadoResponse toResponse(LoteTostado entity);
}
