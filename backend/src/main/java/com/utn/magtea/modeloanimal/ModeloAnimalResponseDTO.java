package com.utn.magtea.modeloanimal;

import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ModeloAnimalResponseDTO(
        Long id,
        String identificador,
        Long poolId,
        Integer poolRango,
        Long camadaId,
        String camadaNombre,
        LocalDate fechaNacimiento,
        SexoRaton sexo,
        LocalDate fechaDia1Inoculacion,
        Integer numCelulasGanglionares,
        Integer numCelulasPurkinje,
        VocalizacionesDTO vocalizaciones,
        TresCamarasDTO tresCamaras,
        boolean necesitaVocalizaciones,
        boolean necesitaTresCamaras,
        boolean activo,
        LocalDateTime createdAt
) {}
