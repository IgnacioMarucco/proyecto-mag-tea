package com.utn.magtea.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteListDTO(
        Long id,
        String codigoNumerico,
        String apellidoTutor,
        String nombreTutor,
        String apellidoNino,
        String nombreNino,
        LocalDate fechaNacimientoNino,
        TipoPaciente tipoPaciente,
        PacienteEstado pacienteEstado,
        LocalDateTime fechaPrimeraVisita,
        LocalDateTime fechaTurnoExtraccion,
        LocalDateTime proximaFechaEvento
) {}
