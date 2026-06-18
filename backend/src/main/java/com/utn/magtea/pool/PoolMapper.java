package com.utn.magtea.pool;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoolMapper {

    @Mapping(target = "cantidadRestante",
             expression = "java(pool.getCantidadTotal() - pool.getCantidadUsada())")
    PoolListDTO toListDTO(Pool pool);

    @Mapping(target = "cajaId", source = "caja.id")
    @Mapping(target = "cantidadRestante",
             expression = "java(pool.getCantidadTotal() - pool.getCantidadUsada())")
    @Mapping(target = "sueroIds",
             expression = "java(pool.getSueros().stream().map(s -> s.getId()).toList())")
    PoolResponseDTO toDTO(Pool pool);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "sueros", ignore = true)
    @Mapping(target = "caja", ignore = true)
    @Mapping(target = "rango", ignore = true)
    @Mapping(target = "cantidadTotal", ignore = true)
    @Mapping(target = "cantidadUsada", ignore = true)
    Pool toEntity(PoolCreateDTO dto);
}
