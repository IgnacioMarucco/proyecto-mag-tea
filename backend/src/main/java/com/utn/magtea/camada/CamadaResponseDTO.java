package com.utn.magtea.camada;

import java.time.LocalDateTime;

public record CamadaResponseDTO(
        Long id,
        String nombre,
        boolean activo,
        LocalDateTime createdAt
) {}
