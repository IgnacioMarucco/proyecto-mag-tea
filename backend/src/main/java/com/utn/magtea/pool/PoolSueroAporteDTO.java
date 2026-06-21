package com.utn.magtea.pool;

public record PoolSueroAporteDTO(
        Long sueroTuboId,
        String posicion,
        String codigoSuero,
        String codigoPaciente,
        double cantidadAportada
) {}
