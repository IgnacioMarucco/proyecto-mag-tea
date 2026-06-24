package com.utn.magtea.camada;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CamadaResponseDTO(
        Long id,
        String nombre,
        LocalDate fechaNacimiento,
        boolean activo,
        LocalDateTime createdAt
) {}
