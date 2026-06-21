package com.utn.magtea.modeloanimal;

import java.time.LocalDate;

public record ModeloAnimalListDTO(
        Long id,
        String identificador,
        Long poolId,
        Integer poolRango,
        String poolCodigo,
        String camadaNombre,
        LocalDate fechaNacimiento,
        SexoRaton sexo,
        boolean necesitaVocalizaciones,
        boolean necesitaTresCamaras
) {}
