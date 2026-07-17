package com.utn.magtea.reporte.dto;

import java.util.List;

public record DonacionesAnaliticaDTO(
        long totalRecaudado,
        long cantidadAprobadas,
        double montoPromedio,
        List<PuntoTemporalDTO> recaudacionPorMes,
        List<DistribucionDTO> porEstado,
        List<DonanteDTO> donantes
) {}
