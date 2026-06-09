package com.utn.magtea.paciente;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PacienteSegundaVisitaDTO(
        @NotNull LocalDate fechaExtraccion
) {}
