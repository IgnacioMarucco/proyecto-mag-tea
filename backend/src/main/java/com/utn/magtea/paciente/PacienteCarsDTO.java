package com.utn.magtea.paciente;

import jakarta.validation.constraints.NotNull;

// Valores válidos por ítem: 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0
public record PacienteCarsDTO(
        @NotNull Double item1,  @NotNull Double item2,  @NotNull Double item3,
        @NotNull Double item4,  @NotNull Double item5,  @NotNull Double item6,
        @NotNull Double item7,  @NotNull Double item8,  @NotNull Double item9,
        @NotNull Double item10, @NotNull Double item11, @NotNull Double item12,
        @NotNull Double item13, @NotNull Double item14, @NotNull Double item15
) {}
