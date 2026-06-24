package com.utn.magtea.reporte.dto;

import java.util.List;

public record AnticuerposDTO(
    long totalConSuero,
    long totalSinSuero,
    double mediaBtu,
    double sdBtu,
    double medianaBtu,
    List<DistribucionDTO> distribucionRangos
) {}
