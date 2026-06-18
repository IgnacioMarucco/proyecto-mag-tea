package com.utn.magtea.suero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record SueroCreateDTO(
        @NotNull(message = "El paciente es obligatorio") Long pacienteId,
        @NotNull(message = "La caja es obligatoria") Long cajaId,
        @NotBlank(message = "Los tubos son obligatorios") String tubos,
        @NotNull(message = "La fecha de extracción es obligatoria") LocalDate fechaExtraccion,
        @NotNull(message = "La cantidad total es obligatoria")
        @Positive(message = "La cantidad total debe ser mayor a 0") Double cantidadTotal,
        @NotNull(message = "El valor de anticuerpos es obligatorio")
        @PositiveOrZero(message = "El valor de anticuerpos no puede ser negativo") Double valorAnticuerpos
) {}
