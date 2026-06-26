package com.utn.magtea.inicio;

import java.time.LocalDate;

public record AlertaConductualItemDTO(
        Long modeloAnimalId,
        String identificador,
        String camadaNombre,
        String tipoTest,
        LocalDate fechaTest,
        int diasRestantes
) {}
