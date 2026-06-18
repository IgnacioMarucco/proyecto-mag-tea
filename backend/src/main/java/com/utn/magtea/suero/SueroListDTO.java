package com.utn.magtea.suero;

import java.time.LocalDate;

public record SueroListDTO(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Integer rango,
        SueroUso uso,
        Double cantidadRestante,
        LocalDate fechaExtraccion
) {}
