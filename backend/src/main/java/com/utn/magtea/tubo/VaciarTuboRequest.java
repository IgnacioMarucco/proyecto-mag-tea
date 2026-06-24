package com.utn.magtea.tubo;

import jakarta.validation.constraints.NotNull;

public record VaciarTuboRequest(
        @NotNull(message = "El motivo de vaciado es obligatorio")
        MotivoVaciado motivo,
        String notas
) {}
