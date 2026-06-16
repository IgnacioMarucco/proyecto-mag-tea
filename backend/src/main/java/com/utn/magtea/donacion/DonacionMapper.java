package com.utn.magtea.donacion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DonacionMapper {

    DonacionResponseDTO toDTO(Donacion donacion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mpPreferenceId", ignore = true)
    @Mapping(target = "mpPaymentId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Donacion toEntity(DonacionCreateDTO dto);
}
