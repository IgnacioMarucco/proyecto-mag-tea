package com.utn.magtea.suero;

import com.utn.magtea.common.MapperHelper;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = {MapperHelper.class})
public interface SueroMapper {

    @Mapping(target = "pacienteId",      source = "paciente.id")
    @Mapping(target = "codigoNumerico",  source = "paciente.codigoNumerico")
    @Mapping(target = "cantidadRestante", source = "tubos", qualifiedByName = "sumCantidadRestante")
    @Mapping(target = "cantidadTotal",    source = "tubos", qualifiedByName = "sumCantidadInicial")
    SueroListDTO toListDTO(Suero suero);

    @Mapping(target = "pacienteId",     source = "paciente.id")
    @Mapping(target = "codigoNumerico", source = "paciente.codigoNumerico")
    @Mapping(target = "cajaId",         source = "caja.id")
    @Mapping(target = "freezer",        source = "caja.freezer")
    @Mapping(target = "cajon",          source = "caja.cajon")
    @Mapping(target = "cajaNumero",     source = "caja.numero")
    @Mapping(target = "cantidadTotal",    source = "tubos", qualifiedByName = "sumCantidadInicial")
    @Mapping(target = "cantidadRestante", source = "tubos", qualifiedByName = "sumCantidadRestante")
    @Mapping(target = "tubos",            source = "tubos", qualifiedByName = "toActiveTuboList")
    SueroResponseDTO toDTO(Suero suero);

    TuboDTO toTuboDTO(Tubo tubo);

    @Named("toActiveTuboList")
    default List<TuboDTO> toActiveTuboList(List<Tubo> tubos) {
        if (tubos == null) return List.of();
        return tubos.stream()
                .filter(t -> t.getCantidadRestante().compareTo(BigDecimal.ZERO) > 0)
                .map(this::toTuboDTO)
                .toList();
    }

    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "activo",   ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "caja",     ignore = true)
    @Mapping(target = "rango",    ignore = true)
    @Mapping(target = "uso",      ignore = true)
    @Mapping(target = "tubos",    ignore = true)
    Suero toEntity(SueroCreateDTO dto);
}
