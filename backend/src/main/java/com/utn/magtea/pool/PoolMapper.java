package com.utn.magtea.pool;

import com.utn.magtea.common.MapperHelper;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {MapperHelper.class})
public interface PoolMapper {

    @Mapping(target = "cantidadTotal",    source = "tubos", qualifiedByName = "sumCantidadInicial")
    @Mapping(target = "cantidadRestante", source = "tubos", qualifiedByName = "sumCantidadRestante")
    @Mapping(target = "cantidadAportes",
             expression = "java((int) pool.getAportes().stream().map(a -> a.getTubo().getSuero().getId()).distinct().count())")
    @Mapping(target = "cajaDescripcion",
             expression = "java(\"Freezer \" + pool.getCaja().getFreezer() + \" › C\" + pool.getCaja().getCajon() + \" › Caja \" + pool.getCaja().getNumero())")
    @Mapping(target = "modelosAnimalesCount",
             expression = "java(pool.getModelosAnimales() != null ? pool.getModelosAnimales().size() : 0)")
    PoolListDTO toListDTO(Pool pool);

    @Mapping(target = "cajaId",      source = "caja.id")
    @Mapping(target = "freezer",     source = "caja.freezer")
    @Mapping(target = "cajon",       source = "caja.cajon")
    @Mapping(target = "cajaNumero",  source = "caja.numero")
    @Mapping(target = "cantidadTotal",    source = "tubos", qualifiedByName = "sumCantidadInicial")
    @Mapping(target = "cantidadRestante", source = "tubos", qualifiedByName = "sumCantidadRestante")
    @Mapping(target = "aportes", expression = "java(toAporteDTOs(pool.getAportes()))")
    PoolResponseDTO toDTO(Pool pool);

    TuboDTO toTuboDTO(Tubo tubo);

    default List<PoolSueroAporteDTO> toAporteDTOs(List<PoolSueroAporte> aportes) {
        if (aportes == null) return List.of();
        return aportes.stream()
            .collect(Collectors.groupingBy(
                a -> a.getTubo().getSuero().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(e -> {
                PoolSueroAporte first = e.getValue().get(0);
                Suero suero = first.getTubo().getSuero();
                BigDecimal total = e.getValue().stream()
                    .map(PoolSueroAporte::getCantidadAportada)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new PoolSueroAporteDTO(
                    suero.getId(),
                    suero.getPaciente().getCodigoNumerico(),
                    suero.getPaciente().getCodigoNumerico(),
                    total,
                    suero.isActivo()
                );
            })
            .toList();
    }

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "codigo",         ignore = true)
    @Mapping(target = "activo",         ignore = true)
    @Mapping(target = "tubos",          ignore = true)
    @Mapping(target = "aportes",        ignore = true)
    @Mapping(target = "modelosAnimales", ignore = true)
    @Mapping(target = "caja",           ignore = true)
    @Mapping(target = "rango",          ignore = true)
    @Mapping(target = "uso",            ignore = true)
    @Mapping(target = "fechaCreacion",  ignore = true)
    Pool toEntity(PoolCreateDTO dto);
}
