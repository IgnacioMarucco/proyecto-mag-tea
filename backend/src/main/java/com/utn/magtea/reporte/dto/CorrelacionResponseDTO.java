package com.utn.magtea.reporte.dto;

import java.util.List;

public record CorrelacionResponseDTO(
    List<CorrelacionPuntoDTO> puntos,
    Double r,
    int n
) {}
