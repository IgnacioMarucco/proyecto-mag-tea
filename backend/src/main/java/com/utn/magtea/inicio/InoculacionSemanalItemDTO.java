package com.utn.magtea.inicio;

import java.time.LocalDate;
import java.util.List;

public record InoculacionSemanalItemDTO(
        Long modeloAnimalId,
        String identificador,
        String camadaNombre,
        String poolCodigo,
        LocalDate fechaDia1,
        List<Integer> diasPendientes,
        List<Integer> diasHechos
) {}
