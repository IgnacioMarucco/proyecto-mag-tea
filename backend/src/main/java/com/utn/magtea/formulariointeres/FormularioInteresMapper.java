package com.utn.magtea.formulariointeres;

import com.utn.magtea.common.MapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MapperHelper.class})
public interface FormularioInteresMapper {

    @Mapping(target = "edadActual", source = "fechaNacimientoNino", qualifiedByName = "calculateAgeYears")
    @Mapping(target = "edadMeses",  source = "fechaNacimientoNino", qualifiedByName = "calculateAgeMonths")
    FormularioInteresResponseDTO toDTO(FormularioInteres entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaContacto", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    FormularioInteres toEntity(FormularioInteresCreateDTO dto);
}
