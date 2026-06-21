package com.utn.magtea.pool;

import com.utn.magtea.suero.SueroUso;

import java.time.LocalDate;

public record PoolListDTO(
        Long id,
        String codigo,
        Integer rango,
        SueroUso uso,
        Double cantidadTotal,
        Double cantidadRestante,
        LocalDate fechaCreacion,
        int cantidadAportes,
        String cajaDescripcion,
        int modelosAnimalesCount
) {}
