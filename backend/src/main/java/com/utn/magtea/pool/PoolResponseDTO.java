package com.utn.magtea.pool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PoolResponseDTO(
        Long id,
        Long cajaId,
        String tubos,
        LocalDate fechaCreacion,
        Integer rango,
        Double cantidadTotal,
        Double cantidadUsada,
        Double cantidadRestante,
        List<Long> sueroIds,
        boolean activo,
        LocalDateTime createdAt
) {}
