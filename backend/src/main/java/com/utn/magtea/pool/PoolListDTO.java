package com.utn.magtea.pool;

import java.time.LocalDate;

public record PoolListDTO(
        Long id,
        Integer rango,
        Double cantidadTotal,
        Double cantidadUsada,
        Double cantidadRestante,
        LocalDate fechaCreacion
) {}
