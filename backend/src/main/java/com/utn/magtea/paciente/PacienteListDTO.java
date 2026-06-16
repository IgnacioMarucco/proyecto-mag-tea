package com.utn.magtea.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteListDTO(
        Long id,
        String apellidoTutor,
        String nombreTutor,
        String apellidoNino,
        String nombreNino,
        LocalDate fechaNacimientoNino,
        TipoPaciente tipoPaciente,
        PacienteEstado pacienteEstado,
        LocalDateTime fechaPrimeraVisita,
        LocalDate fechaExtraccion
) {}
