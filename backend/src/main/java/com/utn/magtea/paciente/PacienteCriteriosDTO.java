package com.utn.magtea.paciente;

public record PacienteCriteriosDTO(
        boolean criterioTEADSMV,
        boolean criterioTGDDSMIV,
        boolean criterioEdad,
        boolean epilepsia,
        boolean paralisisCerebral,
        boolean infeccionesCongenitas,
        boolean lesionesEstructuralesSNC,
        boolean facomatosis,
        boolean patologiasNeurometabolicas,
        boolean lesionesOcupantesEspacioSNC,
        boolean patologiaPsiquiatrica,
        boolean otrosSindromesGeneticos,
        boolean pubertadPrecoz,
        boolean consentimientoFirmado
) {}
