package com.utn.magtea.pool;

import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.TuboDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PoolResponseDTO(
        Long id,
        String codigo,
        Long cajaId,
        List<TuboDTO> tubos,
        LocalDate fechaCreacion,
        Integer rango,
        SueroUso uso,
        Double cantidadTotal,
        Double cantidadRestante,
        List<PoolSueroAporteDTO> aportes,
        boolean activo,
        LocalDateTime createdAt
) {}
