package com.utn.magtea.profesional;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {
    ProfesionalResponseDTO toDTO(Profesional profesional);
    Profesional toEntity(ProfesionalCreateDTO dto);
}
