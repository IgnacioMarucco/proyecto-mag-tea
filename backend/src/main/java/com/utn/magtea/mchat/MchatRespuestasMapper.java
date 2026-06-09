package com.utn.magtea.mchat;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MchatRespuestasMapper {
    MchatRespuestasResponseDTO toDTO(MchatRespuestas respuestas);
}
