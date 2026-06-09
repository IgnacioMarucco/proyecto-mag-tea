package com.utn.magtea.paciente;

public record PacienteVinelandDTO(
        Integer vinelandComunicacion,
        Integer vinelandAutovalimiento,
        Integer vinelandSocial,
        Integer vinelandMotor,
        Integer vinelandCocienteFinal,
        Integer vinelandConductaDesadaptativa,
        Integer vinelandInternalizante,
        Integer vinelandExternalizante
) {}
