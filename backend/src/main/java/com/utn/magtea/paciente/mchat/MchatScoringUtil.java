package com.utn.magtea.paciente.mchat;

import java.util.Set;

public class MchatScoringUtil {

    private static final Set<Integer> INVERTIDAS = Set.of(2, 5, 12);

    public static int calcularScore(boolean[] respuestas) {
        if (respuestas == null || respuestas.length != 20)
            throw new IllegalArgumentException("Se requieren exactamente 20 respuestas");
        int fallas = 0;
        for (int i = 0; i < 20; i++) {
            int n = i + 1;
            boolean valor = respuestas[i];
            if (INVERTIDAS.contains(n) ? valor : !valor) fallas++;
        }
        return fallas;
    }

    private MchatScoringUtil() {}
}
