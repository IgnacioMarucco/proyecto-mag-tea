package com.utn.magtea.modeloanimal;

import java.time.LocalDateTime;

public record ImagenMicroscopiaDTO(
        Long id,
        TipoImagenMicroscopia tipo,
        Long documentoId,
        String urlExterna,
        String descripcion,
        LocalDateTime createdAt
) {}
