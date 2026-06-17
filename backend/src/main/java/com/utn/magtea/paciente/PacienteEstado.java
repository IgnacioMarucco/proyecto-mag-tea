package com.utn.magtea.paciente;

import java.util.Set;

public enum PacienteEstado {
    ADMITIDO,
    MCHAT_RESPONDIDO,
    EXTRACCION_PENDIENTE,
    EXTRACCION_REALIZADA;

    /** Estados que indican que el M-CHAT ya fue completado por la familia. */
    public static final Set<PacienteEstado> MCHAT_COMPLETADOS =
        Set.of(MCHAT_RESPONDIDO, EXTRACCION_PENDIENTE, EXTRACCION_REALIZADA);

    /** Estados que indican que la extracción de sangre está pendiente o realizada. */
    public static final Set<PacienteEstado> CON_EXTRACCION =
        Set.of(EXTRACCION_PENDIENTE, EXTRACCION_REALIZADA);
}
