package com.utn.magtea.modeloanimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ModeloAnimalCreateDTO(
        @NotNull(message = "El pool es obligatorio") Long poolId,
        @NotNull(message = "La camada es obligatoria") Long camadaId,
        @NotNull(message = "El sexo es obligatorio") SexoRaton sexo,
        @Valid List<ModeloAnimalPoolAporteInputDTO> aportes
) {}
