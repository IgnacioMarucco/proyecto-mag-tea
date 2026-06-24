package com.utn.magtea.modeloanimal;

import java.math.BigDecimal;

public record ModeloAnimalPoolAporteDTO(
        Long poolTuboId,
        String posicion,
        BigDecimal cantidadConsumida,
        Integer dia
) {}
