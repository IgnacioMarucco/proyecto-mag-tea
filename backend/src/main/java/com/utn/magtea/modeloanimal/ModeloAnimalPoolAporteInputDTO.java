package com.utn.magtea.modeloanimal;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ModeloAnimalPoolAporteInputDTO(
        @NotNull(message = "El tubo del pool es obligatorio") Long poolTuboId,
        BigDecimal cantidadConsumida,
        Integer dia
) {}
