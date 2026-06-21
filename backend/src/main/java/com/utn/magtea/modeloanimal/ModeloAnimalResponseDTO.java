package com.utn.magtea.modeloanimal;

import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ModeloAnimalResponseDTO(
        Long id,
        String identificador,
        Long poolId,
        Integer poolRango,
        String poolCodigo,
        Long camadaId,
        String camadaNombre,
        LocalDate fechaNacimiento,
        SexoRaton sexo,
        LocalDate fechaDia1Inoculacion,
        Integer numCelulasGanglionares,
        Integer numCelulasPurkinje,
        VocalizacionesDTO vocalizaciones,
        TresCamarasDTO tresCamaras,
        List<ModeloAnimalPoolAporteDTO> aportes,
        boolean necesitaVocalizaciones,
        boolean necesitaTresCamaras,
        boolean activo,
        LocalDateTime createdAt
) {}
