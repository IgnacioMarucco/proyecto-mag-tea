package com.utn.magtea.paciente.mchat;

public record MchatPublicResponseDTO(
        String nombreNino,
        String apellidoNino,
        boolean yaCompletado
) {}
