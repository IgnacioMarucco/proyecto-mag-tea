package com.utn.magtea.caja;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CajaCreateDTO(
        @NotBlank(message = "El freezer es obligatorio") String freezer,
        @NotNull(message = "El cajón es obligatorio")
        @Min(value = 1, message = "El cajón debe ser mayor a 0") Integer cajon,
        @NotNull(message = "El número es obligatorio")
        @Min(value = 1, message = "El número debe ser mayor a 0") Integer numero
) {}
