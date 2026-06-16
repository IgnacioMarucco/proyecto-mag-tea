package com.utn.magtea.reporte.dto;

public record ResumenGeneralDTO(
    long totalFormularios,
    long formulariosContactados,
    long formulariosAdmitidos,
    long pacientesTotal,
    long pacientesProblema,
    long pacientesControl,
    long mchatCompletados,
    long extraccionesRealizadas
) {}
