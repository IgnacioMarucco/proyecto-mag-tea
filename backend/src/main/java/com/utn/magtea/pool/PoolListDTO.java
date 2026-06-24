package com.utn.magtea.pool;

import com.utn.magtea.suero.SueroUso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PoolListDTO(
        Long id,
        String codigo,
        Integer rango,
        SueroUso uso,
        BigDecimal cantidadTotal,
        BigDecimal cantidadRestante,
        LocalDate fechaCreacion,
        int cantidadAportes,
        String cajaDescripcion,
        int modelosAnimalesCount
) {}
