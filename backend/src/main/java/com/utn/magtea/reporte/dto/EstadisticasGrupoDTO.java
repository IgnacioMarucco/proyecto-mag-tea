package com.utn.magtea.reporte.dto;

public record EstadisticasGrupoDTO(
    long nTotal,
    long nConSuero,
    double pctConSuero,
    double mediaBtu,
    double sdBtu
) {}
