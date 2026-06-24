package com.utn.magtea.reporte.dto;

public record DashboardAnaliticaDTO(
        ResumenGeneralDTO resumen,
        EmbudoDTO embudo,
        DemograficoDTO demografico,
        MchatAnaliticaDTO mchat,
        CarsAnaliticaDTO cars,
        VinelandAnaliticaDTO vineland,
        AnticuerposDTO anticuerpos,
        ComparacionGruposDTO comparacion
) {}
