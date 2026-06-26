package com.utn.magtea.paciente;

public record PacienteConsentimientoDTO(
        boolean consentimientoFirmado,
        Long documentoId
) {}
