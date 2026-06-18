package com.utn.magtea.modeloanimal.estudios;

import com.utn.magtea.modeloanimal.VusBanda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VocalizacionesDTO(
        @NotNull(message = "La muestra 1 (kHz) es obligatoria")
        @Positive(message = "La muestra 1 debe ser mayor a 0") Double muestra1Khz,
        @NotNull(message = "La muestra 2 (kHz) es obligatoria")
        @Positive(message = "La muestra 2 debe ser mayor a 0") Double muestra2Khz,
        VusBanda vusBanda1,
        VusBanda vusBanda2
) {}
