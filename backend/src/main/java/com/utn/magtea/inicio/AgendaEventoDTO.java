package com.utn.magtea.inicio;

import java.time.LocalDate;

public record AgendaEventoDTO(
        LocalDate fecha,
        String categoria,
        String identificador,
        Long entityId,
        String entityPath
) {}
