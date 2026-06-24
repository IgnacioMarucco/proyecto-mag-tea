package com.utn.magtea.modeloanimal;

public enum SocializacionResultado {
    NORMAL,
    FALTA_SOCIALIZACION;

    public static SocializacionResultado from(Double tiempoA, Double tiempoB) {
        if (tiempoA == null || tiempoB == null || tiempoB == 0) return null;
        return tiempoA / tiempoB <= 1.0 ? FALTA_SOCIALIZACION : NORMAL;
    }
}
