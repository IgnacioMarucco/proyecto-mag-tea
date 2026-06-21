package com.utn.magtea.modeloanimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ModeloAnimalCreateDTO(
        @NotBlank(message = "El identificador es obligatorio") String identificador,
        @NotNull(message = "El pool es obligatorio") Long poolId,
        @NotNull(message = "La camada es obligatoria") Long camadaId,
        @NotNull(message = "La fecha de nacimiento es obligatoria") LocalDate fechaNacimiento,
        @NotNull(message = "El sexo es obligatorio") SexoRaton sexo,
        @NotNull(message = "La fecha de inoculación es obligatoria") LocalDate fechaDia1Inoculacion,
        @Valid List<ModeloAnimalPoolAporteInputDTO> aportes
) {}
