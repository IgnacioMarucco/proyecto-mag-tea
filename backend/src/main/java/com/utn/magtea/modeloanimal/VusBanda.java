package com.utn.magtea.modeloanimal;

public enum VusBanda {
    AVERSIVA,
    APETITIVA;

    private static final double LIMITE_KHZ = 50.0;

    public static VusBanda from(Double khz) {
        if (khz == null) return null;
        return khz <= LIMITE_KHZ ? AVERSIVA : APETITIVA;
    }
}
