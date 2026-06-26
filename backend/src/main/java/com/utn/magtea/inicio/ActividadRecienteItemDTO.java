package com.utn.magtea.inicio;

import java.time.LocalDateTime;

public record ActividadRecienteItemDTO(
        String tipo,
        String descripcion,
        LocalDateTime fecha,
        Long entityId,
        String identificador,
        String entityPath,
        String nombreProfesional,
        String rol
) {}
