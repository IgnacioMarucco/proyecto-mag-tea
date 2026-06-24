package com.utn.magtea.suero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SueroListDTO(
        Long id,
        Long pacienteId,
        String codigoNumerico,
        BigDecimal valorAnticuerpos,
        Integer rango,
        SueroUso uso,
        BigDecimal cantidadRestante,
        BigDecimal cantidadTotal,
        LocalDate fechaExtraccion
) {}
