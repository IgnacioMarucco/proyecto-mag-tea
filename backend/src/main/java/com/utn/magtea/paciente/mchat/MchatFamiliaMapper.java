package com.utn.magtea.paciente.mchat;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MchatFamiliaMapper {
    MchatFamiliaResponseDTO toDTO(MchatFamilia familia);
}
