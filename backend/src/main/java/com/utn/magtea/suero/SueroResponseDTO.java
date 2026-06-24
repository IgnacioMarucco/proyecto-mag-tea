package com.utn.magtea.suero;

import com.utn.magtea.tubo.TuboDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SueroResponseDTO(
        Long id,
        Long pacienteId,
        String codigoNumerico,
        Long cajaId,
        String freezer,
        Integer cajon,
        Integer cajaNumero,
        List<TuboDTO> tubos,
        LocalDate fechaExtraccion,
        BigDecimal cantidadTotal,
        BigDecimal cantidadRestante,
        BigDecimal valorAnticuerpos,
        Integer rango,
        SueroUso uso,
        boolean activo,
        LocalDateTime createdAt
) {}
