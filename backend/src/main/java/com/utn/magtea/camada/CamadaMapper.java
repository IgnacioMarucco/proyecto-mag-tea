package com.utn.magtea.camada;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CamadaMapper {

    CamadaListDTO toListDTO(Camada camada);

    CamadaResponseDTO toDTO(Camada camada);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Camada toEntity(CamadaCreateDTO dto);
}
