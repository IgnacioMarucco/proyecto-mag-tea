package com.utn.magtea.paciente;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PacientePrimeraVisitaDTO(
        @NotNull LocalDateTime fechaPrimeraVisita
) {}
