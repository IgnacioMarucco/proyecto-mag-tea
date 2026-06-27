package com.utn.magtea.common;

import java.math.BigDecimal;

public final class DomainConstants {

    private DomainConstants() {}

    /** Volumen de suero inoculado por ratón en cada dosis (mL). */
    public static final BigDecimal ML_POR_RATON = new BigDecimal("0.20");

    /** Día desde nacimiento de camada en que se realizan las vocalizaciones ultrasónicas. */
    public static final int DIA_VOCALIZACIONES  = 7;
    /** Día desde nacimiento de camada en que se realizan tres cámaras y microscopía. */
    public static final int DIA_TRES_CAMARAS    = 21;
    /** Días de anticipación con que el sistema emite alertas conductuales. */
    public static final int VENTANA_ALERTA_DIAS = 2;
}
