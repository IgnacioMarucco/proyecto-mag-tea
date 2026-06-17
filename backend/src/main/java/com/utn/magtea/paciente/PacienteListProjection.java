package com.utn.magtea.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PacienteListProjection {
    Long getId();
    String getApellidoTutor();
    String getNombreTutor();
    String getApellidoNino();
    String getNombreNino();
    LocalDate getFechaNacimientoNino();
    TipoPaciente getTipoPaciente();
    PacienteEstado getEstadoClinico();
    LocalDateTime getFechaPrimeraVisita();
    LocalDate getFechaExtraccion();
}
