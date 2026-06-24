package com.utn.magtea.tubo;

import java.math.BigDecimal;

public record TuboDTO(
        Long id,
        String posicion,
        BigDecimal cantidadInicial,
        BigDecimal cantidadRestante,
        MotivoVaciado motivoVaciado
) {}
