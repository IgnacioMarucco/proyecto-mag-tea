package com.utn.magtea.suero;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = SueroUso.class)
public interface SueroMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNombre",
             expression = "java(suero.getPaciente().getApellidoNino() + \", \" + suero.getPaciente().getNombreNino())")
    @Mapping(target = "uso",
             expression = "java(suero.getValorAnticuerpos() == 0.0 ? SueroUso.CONTROL : SueroUso.PROBLEMA)")
    @Mapping(target = "cantidadRestante",
             expression = "java(suero.getCantidadTotal() - suero.getCantidadUsada())")
    SueroListDTO toListDTO(Suero suero);

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNombre",
             expression = "java(suero.getPaciente().getApellidoNino() + \", \" + suero.getPaciente().getNombreNino())")
    @Mapping(target = "cajaId", source = "caja.id")
    @Mapping(target = "uso",
             expression = "java(suero.getValorAnticuerpos() == 0.0 ? SueroUso.CONTROL : SueroUso.PROBLEMA)")
    @Mapping(target = "cantidadRestante",
             expression = "java(suero.getCantidadTotal() - suero.getCantidadUsada())")
    SueroResponseDTO toDTO(Suero suero);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "caja", ignore = true)
    @Mapping(target = "rango", ignore = true)
    @Mapping(target = "cantidadUsada", ignore = true)
    Suero toEntity(SueroCreateDTO dto);
}
