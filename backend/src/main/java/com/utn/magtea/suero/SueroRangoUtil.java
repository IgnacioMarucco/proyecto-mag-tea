package com.utn.magtea.suero;

/**
 * Calcula el rango BTU de un suero según el valor de anticuerpos.
 * Rango 0 (CONTROL): 0–1313 BTU
 * Rango 1: 1314–2500 BTU
 * Rango 2: 2501–8000 BTU
 * Rango 3: > 8000 BTU
 */
public final class SueroRangoUtil {

    private SueroRangoUtil() {}

    public static Integer calcularRango(Double valorAnticuerpos) {
        if (valorAnticuerpos == null) return null;
        if (valorAnticuerpos <= 1313) return 0;
        if (valorAnticuerpos <= 2500) return 1;
        if (valorAnticuerpos <= 8000) return 2;
        return 3;
    }
}
