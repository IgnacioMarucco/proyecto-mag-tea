package com.utn.magtea.modeloanimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ModeloAnimalMicroscopiaDTO(
        @NotNull(message = "El número de células ganglionares es obligatorio")
        @PositiveOrZero Integer numCelulasGanglionares,
        @NotNull(message = "El número de células de Purkinje es obligatorio")
        @PositiveOrZero Integer numCelulasPurkinje
) {}
