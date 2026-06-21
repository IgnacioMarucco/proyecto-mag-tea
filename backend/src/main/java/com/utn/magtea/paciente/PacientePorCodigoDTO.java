package com.utn.magtea.paciente;

import java.time.LocalDate;

public record PacientePorCodigoDTO(
        Long id,
        String codigoNumerico,
        String nombreNino,
        String apellidoNino,
        LocalDate fechaExtraccion,
        TipoPaciente tipoPaciente
) {}
