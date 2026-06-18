package com.utn.magtea.suero;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SueroResponseDTO(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long cajaId,
        String tubos,
        LocalDate fechaExtraccion,
        Double cantidadTotal,
        Double cantidadUsada,
        Double cantidadRestante,
        Double valorAnticuerpos,
        Integer rango,
        SueroUso uso,
        boolean activo,
        LocalDateTime createdAt
) {}
