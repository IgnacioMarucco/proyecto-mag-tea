package com.utn.magtea.suero;

import java.math.BigDecimal;

public record SueroDisponibilidadDTO(
        SueroUso uso,
        Integer rango,
        Long cantidadSueros,
        BigDecimal mlDisponibles,
        Integer ratonesPosibles
) {}
