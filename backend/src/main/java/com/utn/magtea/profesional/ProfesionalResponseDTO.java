package com.utn.magtea.profesional;

import java.time.LocalDateTime;

public record ProfesionalResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        Role role,
        boolean activo,
        LocalDateTime createdAt
) {}
