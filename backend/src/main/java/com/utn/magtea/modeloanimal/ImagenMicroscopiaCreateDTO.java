package com.utn.magtea.modeloanimal;

import jakarta.validation.constraints.NotNull;

public record ImagenMicroscopiaCreateDTO(
        @NotNull(message = "El tipo de imagen es obligatorio") TipoImagenMicroscopia tipo,
        Long documentoId,
        String urlExterna,
        String descripcion
) {}
