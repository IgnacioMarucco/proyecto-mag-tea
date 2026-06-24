package com.utn.magtea.paciente.criterios;

import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.TipoPaciente;

public final class CriteriosUtil {

    private CriteriosUtil() {}

    public static CriteriosAptitud calcularAptitud(Paciente paciente) {
        Criterios c = paciente.getCriterios();
        if (c == null) return null;
        boolean exclusion = c.isEpilepsia() || c.isParalisisCerebral() ||
                c.isInfeccionesCongenitas() || c.isLesionesEstructuralesSNC() ||
                c.isFacomatosis() || c.isPatologiasNeurometabolicas() ||
                c.isLesionesOcupantesEspacioSNC() || c.isPatologiaPsiquiatrica() ||
                c.isOtrosSindromesGeneticos() || c.isPubertadPrecoz();
        if (exclusion) return CriteriosAptitud.EXCLUIDO;
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            if (!c.isCriterioTEADSMV() && !c.isCriterioTGDDSMIV() && c.isCriterioEdad())
                return CriteriosAptitud.APTO;
        } else {
            if (c.isCriterioTEADSMV() && c.isCriterioTGDDSMIV() && c.isCriterioEdad())
                return CriteriosAptitud.APTO;
        }
        return CriteriosAptitud.INCOMPLETO;
    }
}
