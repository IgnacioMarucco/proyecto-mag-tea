package com.utn.magtea.tubo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TuboInputDTO(
        @NotBlank(message = "La posición del tubo es obligatoria") String posicion,
        // Cantidad ACTUAL del tubo (lo que contiene ahora). Al crear equivale a la cantidad
        // inicial (sin consumo); al editar, el service recalcula la inicial preservando lo usado.
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0") BigDecimal cantidad
) {}
