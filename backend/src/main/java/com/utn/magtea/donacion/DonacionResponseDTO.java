package com.utn.magtea.donacion;

import java.time.LocalDateTime;

public record DonacionResponseDTO(
        Long id,
        Long monto,
        String donante,
        String correo,
        EstadoDonacion estado,
        LocalDateTime createdAt
) {}
