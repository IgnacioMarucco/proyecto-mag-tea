package com.utn.magtea.pool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PoolUpdateDTO(
        @NotNull(message = "La caja es obligatoria") Long cajaId,
        @NotBlank(message = "Los tubos son obligatorios") String tubos,
        @NotNull(message = "La fecha de creación es obligatoria") LocalDate fechaCreacion
) {}
