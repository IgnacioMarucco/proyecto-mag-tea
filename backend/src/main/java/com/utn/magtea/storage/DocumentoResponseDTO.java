package com.utn.magtea.storage;

import java.time.LocalDateTime;

public record DocumentoResponseDTO(
        Long id,
        String bucket,
        String clave,
        String nombreOriginal,
        String mimeType,
        Long tamanio,
        LocalDateTime createdAt
) {}
