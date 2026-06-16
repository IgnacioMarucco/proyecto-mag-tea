package com.utn.magtea.paciente.cars;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Valores válidos por ítem: 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0
public record CarsDTO(
        @NotNull BigDecimal item1,  @NotNull BigDecimal item2,  @NotNull BigDecimal item3,
        @NotNull BigDecimal item4,  @NotNull BigDecimal item5,  @NotNull BigDecimal item6,
        @NotNull BigDecimal item7,  @NotNull BigDecimal item8,  @NotNull BigDecimal item9,
        @NotNull BigDecimal item10, @NotNull BigDecimal item11, @NotNull BigDecimal item12,
        @NotNull BigDecimal item13, @NotNull BigDecimal item14, @NotNull BigDecimal item15,
        String obs1,  String obs2,  String obs3,  String obs4,  String obs5,
        String obs6,  String obs7,  String obs8,  String obs9,  String obs10,
        String obs11, String obs12, String obs13, String obs14, String obs15,
        BigDecimal tScore,
        Integer percentil
) {}
