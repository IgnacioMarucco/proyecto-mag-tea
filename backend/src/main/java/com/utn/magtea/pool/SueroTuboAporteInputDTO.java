package com.utn.magtea.pool;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SueroTuboAporteInputDTO(
        @NotNull(message = "El tubo de suero es obligatorio") Long sueroTuboId,
        @NotNull(message = "La cantidad aportada es obligatoria")
        @Positive(message = "La cantidad aportada debe ser mayor a 0") BigDecimal cantidadAportada
) {}
