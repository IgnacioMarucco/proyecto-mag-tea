package com.utn.magtea.paciente.vineland;

public record VinelandDTO(
        Integer comunicacion,
        Integer autovalimiento,
        Integer social,
        Integer motor,
        Integer cocienteFinal,
        Integer conductaDesadaptativa,
        Integer internalizante,
        Integer externalizante
) {}
