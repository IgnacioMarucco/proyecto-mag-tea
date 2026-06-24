package com.utn.magtea.pool;

import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.TuboDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PoolResponseDTO(
        Long id,
        String codigo,
        Long cajaId,
        String freezer,
        Integer cajon,
        Integer cajaNumero,
        List<TuboDTO> tubos,
        LocalDate fechaCreacion,
        Integer rango,
        SueroUso uso,
        BigDecimal cantidadTotal,
        BigDecimal cantidadRestante,
        List<PoolSueroAporteDTO> aportes,
        boolean activo,
        LocalDateTime createdAt
) {}
