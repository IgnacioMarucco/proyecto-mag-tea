package com.utn.magtea.suero;

import java.math.BigDecimal;

/**
 * Calcula el rango BTU de un suero según el valor de anticuerpos.
 * Rango 0 (CONTROL): 0–1313 BTU
 * Rango 1: 1314–2500 BTU
 * Rango 2: 2501–8000 BTU
 * Rango 3: > 8000 BTU
 */
public final class SueroRangoUtil {

    private static final BigDecimal RANGO_0_MAX = new BigDecimal("1313");
    private static final BigDecimal RANGO_1_MAX = new BigDecimal("2500");
    private static final BigDecimal RANGO_2_MAX = new BigDecimal("8000");

    private SueroRangoUtil() {}

    public static Integer calcularRango(BigDecimal valorAnticuerpos) {
        if (valorAnticuerpos == null) return null;
        if (valorAnticuerpos.compareTo(RANGO_0_MAX) <= 0) return 0;
        if (valorAnticuerpos.compareTo(RANGO_1_MAX) <= 0) return 1;
        if (valorAnticuerpos.compareTo(RANGO_2_MAX) <= 0) return 2;
        return 3;
    }
}
