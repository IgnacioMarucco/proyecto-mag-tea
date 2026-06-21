package com.utn.magtea.suero;

import java.time.LocalDate;

public record SueroListDTO(
        Long id,
        Long pacienteId,
        String codigoNumerico,
        Double valorAnticuerpos,
        Integer rango,
        SueroUso uso,
        Double cantidadRestante,
        Double cantidadTotal,
        LocalDate fechaExtraccion
) {}
