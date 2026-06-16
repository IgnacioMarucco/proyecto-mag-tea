package com.utn.magtea.reporte.dto;

import java.util.List;

public record CarsAnaliticaDTO(
    List<DistribucionDTO> distribucionRawScore,
    long minimoNoTea,
    long leveModerado,
    long severo,
    double mediaRawScore,
    double sdRawScore,
    long totalConCars
) {}
