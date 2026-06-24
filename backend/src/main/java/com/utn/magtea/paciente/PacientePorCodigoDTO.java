package com.utn.magtea.paciente;

import java.time.LocalDateTime;

public record PacientePorCodigoDTO(
        Long id,
        String codigoNumerico,
        String nombreNino,
        String apellidoNino,
        LocalDateTime fechaTurnoExtraccion,
        TipoPaciente tipoPaciente
) {}
