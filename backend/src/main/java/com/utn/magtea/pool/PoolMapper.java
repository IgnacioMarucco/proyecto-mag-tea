package com.utn.magtea.pool;

import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoolMapper {

    @Mapping(target = "cantidadTotal",
             expression = "java(pool.getTubos().stream().mapToDouble(t -> t.getCantidadInicial()).sum())")
    @Mapping(target = "cantidadRestante",
             expression = "java(pool.getTubos().stream().mapToDouble(t -> t.getCantidadRestante()).sum())")
    @Mapping(target = "cantidadAportes",
             expression = "java(pool.getAportes().size())")
    @Mapping(target = "cajaDescripcion",
             expression = "java(\"Freezer \" + pool.getCaja().getFreezer() + \" › C\" + pool.getCaja().getCajon() + \" › Caja \" + pool.getCaja().getNumero())")
    @Mapping(target = "modelosAnimalesCount",
             expression = "java(pool.getModelosAnimales() != null ? pool.getModelosAnimales().size() : 0)")
    PoolListDTO toListDTO(Pool pool);

    @Mapping(target = "cajaId", source = "caja.id")
    @Mapping(target = "cantidadTotal",
             expression = "java(pool.getTubos().stream().mapToDouble(t -> t.getCantidadInicial()).sum())")
    @Mapping(target = "cantidadRestante",
             expression = "java(pool.getTubos().stream().mapToDouble(t -> t.getCantidadRestante()).sum())")
    PoolResponseDTO toDTO(Pool pool);

    TuboDTO toTuboDTO(Tubo tubo);

    @Mapping(target = "sueroTuboId",    source = "tubo.id")
    @Mapping(target = "posicion",       source = "tubo.posicion")
    @Mapping(target = "codigoSuero",
             expression = "java(aporte.getTubo().getSuero().getPaciente().getCodigoNumerico())")
    @Mapping(target = "codigoPaciente",
             expression = "java(aporte.getTubo().getSuero().getPaciente().getCodigoNumerico())")
    PoolSueroAporteDTO toAporteDTO(PoolSueroAporte aporte);

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "codigo",         ignore = true)
    @Mapping(target = "activo",         ignore = true)
    @Mapping(target = "tubos",          ignore = true)
    @Mapping(target = "aportes",        ignore = true)
    @Mapping(target = "modelosAnimales", ignore = true)
    @Mapping(target = "caja",           ignore = true)
    @Mapping(target = "rango",          ignore = true)
    @Mapping(target = "uso",            ignore = true)
    Pool toEntity(PoolCreateDTO dto);
}
