package com.utn.magtea.modeloanimal;

import com.utn.magtea.suero.SueroUso;

import java.time.LocalDate;

public record ModeloAnimalListDTO(
        Long id,
        String identificador,
        Long poolId,
        Integer poolRango,
        String poolCodigo,
        SueroUso poolUso,
        String camadaNombre,
        LocalDate fechaNacimiento,
        SexoRaton sexo,
        int aportesCount,
        boolean necesitaVocalizaciones,
        boolean necesitaTresCamaras,
        EstadoProtocolo estadoProtocolo,
        LocalDate fechaProximoEvento
) {}
