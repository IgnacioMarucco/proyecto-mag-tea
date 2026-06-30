package com.utn.magtea.pool;

import java.math.BigDecimal;

public record PoolSueroAporteDTO(
        Long sueroId,
        String codigoSuero,
        String codigoPaciente,
        BigDecimal cantidadAportada,
        boolean sueroActivo
) {}
