package com.utn.magtea.suero;

public record SueroDisponibilidadDTO(
        SueroUso uso,
        Integer rango,
        Long cantidadSueros,
        Double mlDisponibles,
        Integer ratonesPosibles
) {}
