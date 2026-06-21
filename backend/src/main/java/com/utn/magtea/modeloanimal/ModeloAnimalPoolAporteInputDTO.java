package com.utn.magtea.modeloanimal;

import jakarta.validation.constraints.NotNull;

public record ModeloAnimalPoolAporteInputDTO(
        @NotNull(message = "El tubo del pool es obligatorio") Long poolTuboId,
        Double cantidadConsumida,
        Integer dia
) {}
