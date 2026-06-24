package com.utn.magtea.modeloanimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ModeloAnimalInoculacionDTO(
        @NotNull(message = "La fecha del día 1 de inoculación es obligatoria")
        LocalDate fechaDia1Inoculacion,
        @Valid List<ModeloAnimalPoolAporteInputDTO> aportes
) {}
