package com.utn.magtea.formulariointeres;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FormularioInteresMapper {

    @Mapping(
        target = "edadActual",
        expression = "java(entity.getFechaNacimientoNino() != null ? java.time.Period.between(entity.getFechaNacimientoNino(), java.time.LocalDate.now()).getYears() : null)"
    )
    @Mapping(
        target = "edadMeses",
        expression = "java(entity.getFechaNacimientoNino() != null ? java.time.Period.between(entity.getFechaNacimientoNino(), java.time.LocalDate.now()).getMonths() : null)"
    )
    FormularioInteresResponseDTO toDTO(FormularioInteres entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaContacto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    FormularioInteres toEntity(FormularioInteresCreateDTO dto);
}
