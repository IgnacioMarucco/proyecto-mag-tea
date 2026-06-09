package com.utn.magtea.profesional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {
    ProfesionalResponseDTO toDTO(Profesional profesional);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    Profesional toEntity(ProfesionalCreateDTO dto);
}
