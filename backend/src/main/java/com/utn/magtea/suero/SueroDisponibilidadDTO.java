package com.utn.magtea.suero;

public record SueroDisponibilidadDTO(
        Integer rango,
        Long cantidadSueros,
        Double mlDisponibles,
        Integer ratonesPosibles
) {}
