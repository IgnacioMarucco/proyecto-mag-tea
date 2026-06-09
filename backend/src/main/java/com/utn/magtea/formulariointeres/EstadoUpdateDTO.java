package com.utn.magtea.formulariointeres;

import jakarta.validation.constraints.NotNull;

public record EstadoUpdateDTO(
        @NotNull(message = "El estado es obligatorio") EstadoFormulario estado
) {}
