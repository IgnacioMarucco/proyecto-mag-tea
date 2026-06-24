package com.utn.magtea.reporte.dto;

public record ComparacionGruposDTO(
    EstadisticasGrupoDTO problema,
    EstadisticasGrupoDTO control,
    Double pValue,
    Double cohenD
) {}
