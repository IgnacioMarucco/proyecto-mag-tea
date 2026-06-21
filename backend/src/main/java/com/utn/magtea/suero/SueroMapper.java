package com.utn.magtea.suero;

import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SueroMapper {

    @Mapping(target = "pacienteId",      source = "paciente.id")
    @Mapping(target = "codigoNumerico",  source = "paciente.codigoNumerico")
    @Mapping(target = "cantidadRestante",
             expression = "java(suero.getTubos().stream().mapToDouble(t -> t.getCantidadRestante()).sum())")
    @Mapping(target = "cantidadTotal",
             expression = "java(suero.getTubos().stream().mapToDouble(t -> t.getCantidadInicial()).sum())")
    SueroListDTO toListDTO(Suero suero);

    @Mapping(target = "pacienteId",     source = "paciente.id")
    @Mapping(target = "codigoNumerico", source = "paciente.codigoNumerico")
    @Mapping(target = "cajaId",         source = "caja.id")
    @Mapping(target = "freezer",        source = "caja.freezer")
    @Mapping(target = "cajon",          source = "caja.cajon")
    @Mapping(target = "cajaNumero",     source = "caja.numero")
    @Mapping(target = "cantidadTotal",
             expression = "java(suero.getTubos().stream().mapToDouble(t -> t.getCantidadInicial()).sum())")
    @Mapping(target = "cantidadRestante",
             expression = "java(suero.getTubos().stream().mapToDouble(t -> t.getCantidadRestante()).sum())")
    SueroResponseDTO toDTO(Suero suero);

    TuboDTO toTuboDTO(Tubo tubo);

    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "activo",   ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "caja",     ignore = true)
    @Mapping(target = "rango",    ignore = true)
    @Mapping(target = "uso",      ignore = true)
    @Mapping(target = "tubos",    ignore = true)
    Suero toEntity(SueroCreateDTO dto);
}
