package com.utn.magtea.paciente;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PacienteSegundaVisitaDTO(
        @NotNull LocalDateTime fechaTurnoExtraccion
) {}
