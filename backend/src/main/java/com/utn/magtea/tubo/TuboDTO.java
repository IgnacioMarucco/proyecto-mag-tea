package com.utn.magtea.tubo;

public record TuboDTO(
        Long id,
        String posicion,
        double cantidadInicial,
        double cantidadRestante
) {}
