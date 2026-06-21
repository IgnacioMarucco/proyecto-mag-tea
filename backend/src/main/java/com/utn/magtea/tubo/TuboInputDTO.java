package com.utn.magtea.tubo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TuboInputDTO(
        @NotBlank(message = "La posición del tubo es obligatoria") String posicion,
        @NotNull(message = "La cantidad inicial es obligatoria")
        @Positive(message = "La cantidad inicial debe ser mayor a 0") Double cantidadInicial
) {}
