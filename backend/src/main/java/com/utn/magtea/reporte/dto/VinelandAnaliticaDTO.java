package com.utn.magtea.reporte.dto;

public record VinelandAnaliticaDTO(
    double mediaComunicacion,
    double mediaAutovalimiento,
    double mediaSocial,
    double mediaMotor,
    double mediaCocienteFinal,
    double sdCocienteFinal,
    Double mediaConductaDesadaptativa,
    Double mediaInternalizante,
    Double mediaExternalizante,
    long totalConVineland
) {}
