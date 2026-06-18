package com.utn.magtea.caja;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CajaMapper {

    CajaListDTO toListDTO(Caja caja);

    CajaResponseDTO toDTO(Caja caja);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Caja toEntity(CajaCreateDTO dto);
}
