package com.utn.magtea.paciente.mchat;

public enum MchatRiesgo {
    BAJO_RIESGO, MEDIANO_RIESGO, ALTO_RIESGO;

    public static MchatRiesgo from(Integer scoreTotal) {
        if (scoreTotal == null) return null;
        if (scoreTotal <= 2) return BAJO_RIESGO;
        if (scoreTotal <= 7) return MEDIANO_RIESGO;
        return ALTO_RIESGO;
    }
}
