package com.utn.magtea.caja;

import java.time.LocalDateTime;

public record CajaResponseDTO(
        Long id,
        String freezer,
        Integer cajon,
        Integer numero,
        boolean activo,
        LocalDateTime createdAt
) {}
