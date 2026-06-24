package com.utn.magtea.paciente.cars;

import java.math.BigDecimal;

public enum CarsResultado {
    MINIMO_NO_TEA, LEVE_MODERADO, SEVERO;

    public static final BigDecimal CUTOFF_SEVERO = new BigDecimal("37");

    public static CarsResultado from(BigDecimal rawScore) {
        if (rawScore == null) return null;
        if (rawScore.compareTo(BigDecimal.valueOf(30)) < 0) return MINIMO_NO_TEA;
        if (rawScore.compareTo(CUTOFF_SEVERO) < 0) return LEVE_MODERADO;
        return SEVERO;
    }
}
