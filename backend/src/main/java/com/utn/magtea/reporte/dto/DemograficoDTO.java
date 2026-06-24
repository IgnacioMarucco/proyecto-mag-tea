package com.utn.magtea.reporte.dto;

import java.util.List;

public record DemograficoDTO(
    List<DistribucionDTO> sexo,
    List<DistribucionDTO> fuenteDerivacion,
    List<DistribucionDTO> distribucionEtaria
) {}
