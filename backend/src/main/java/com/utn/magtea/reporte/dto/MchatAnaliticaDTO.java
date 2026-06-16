package com.utn.magtea.reporte.dto;

import java.util.List;

public record MchatAnaliticaDTO(
    List<DistribucionDTO> distribucionScores,
    List<DistribucionDTO> resultadoFinal,
    long totalConMchat,
    long riesgoMedio,
    long riesgoMedioConSeguimiento,
    long riesgoMedioPositiva,
    long riesgoMedioNegativa,
    List<DistribucionDTO> itemsFalladosTamizaje,
    long totalConSeguimiento,
    List<DistribucionDTO> itemsFalladosSeguimiento
) {}
