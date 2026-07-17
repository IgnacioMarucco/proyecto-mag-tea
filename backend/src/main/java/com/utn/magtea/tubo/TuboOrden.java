package com.utn.magtea.tubo;

import java.util.Comparator;

/**
 * Orden natural de tubos por posición de grilla ("A1", "A2", ..., "A10", "B1"),
 * separando el prefijo de letras del sufijo numérico para que A10 no quede antes que A2.
 * Los tubos sin posición (vaciados) quedan al final.
 */
public final class TuboOrden {

    private TuboOrden() {}

    public static final Comparator<Tubo> POR_POSICION =
            Comparator.comparing(Tubo::getPosicion, Comparator.nullsLast(TuboOrden::compararPosicion));

    private static int compararPosicion(String a, String b) {
        int cmp = letras(a).compareTo(letras(b));
        if (cmp != 0) return cmp;
        return Integer.compare(numero(a), numero(b));
    }

    private static String letras(String posicion) {
        int i = 0;
        while (i < posicion.length() && Character.isLetter(posicion.charAt(i))) i++;
        return posicion.substring(0, i);
    }

    private static int numero(String posicion) {
        int i = 0;
        while (i < posicion.length() && Character.isLetter(posicion.charAt(i))) i++;
        String num = posicion.substring(i).replaceAll("[^0-9]", "");
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}
