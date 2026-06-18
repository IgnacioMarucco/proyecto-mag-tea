package com.utn.magtea.pool;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SueroAportDTO(
        @NotNull(message = "El suero es obligatorio") Long sueroId,
        @NotNull(message = "La cantidad aportada es obligatoria")
        @Positive(message = "La cantidad aportada debe ser mayor a 0") Double cantidadAportada
) {}
