package com.utn.magtea.paciente;

import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.paciente.cars.CarsItemsResponseDTO;
import com.utn.magtea.paciente.cars.CarsResultado;
import com.utn.magtea.paciente.criterios.CriteriosAptitud;
import com.utn.magtea.paciente.mchatseguimiento.MchatEstado;
import com.utn.magtea.paciente.mchatseguimiento.MchatResultadoFinal;
import com.utn.magtea.paciente.mchatseguimiento.MchatRiesgo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponseDTO(
        Long id,
        Long formularioInteresId,
        String codigoNumerico,
        LocalDate fechaContacto,
        String apellidoTutor,
        String nombreTutor,
        String correoTutor,
        String telefono,
        String apellidoNino,
        String nombreNino,
        LocalDate fechaNacimientoNino,
        Integer edadActual,
        Integer edadMeses,
        ComoConocioProyecto comoConocioProyecto,
        String otroComoConocio,
        Sexo sexo,
        TipoPaciente tipoPaciente,
        LocalDateTime fechaPrimeraVisita,
        boolean consentimientoFirmado,
        String notas,
        // Estado calculado del paciente en el protocolo
        PacienteEstado pacienteEstado,
        // Estado del M-CHAT (calculado)
        MchatEstado mchatEstado,
        // Criterios
        boolean criteriosRegistrados,
        CriteriosAptitud criteriosAptitud,
        // Criterios inclusión
        boolean criterioTEADSMV,
        boolean criterioTGDDSMIV,
        boolean criterioEdad,
        // Criterios exclusión
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
        // M-CHAT
        Integer mchatScoreTotal,
        Integer mchatSeguimientoFallas,
        MchatResultadoFinal mchatResultadoFinal,
        MchatRiesgo mchatResultado,
        // Respuestas individuales del seguimiento (null si no aplica)
        Boolean seguimientoItem1,  Boolean seguimientoItem2,  Boolean seguimientoItem3,
        Boolean seguimientoItem4,  Boolean seguimientoItem5,  Boolean seguimientoItem6,
        Boolean seguimientoItem7,  Boolean seguimientoItem8,  Boolean seguimientoItem9,
        Boolean seguimientoItem10, Boolean seguimientoItem11, Boolean seguimientoItem12,
        Boolean seguimientoItem13, Boolean seguimientoItem14, Boolean seguimientoItem15,
        Boolean seguimientoItem16, Boolean seguimientoItem17, Boolean seguimientoItem18,
        Boolean seguimientoItem19, Boolean seguimientoItem20,
        // CARS-2
        BigDecimal carsRawScore,
        BigDecimal carsTScore,
        Integer carsPercentil,
        CarsResultado carsResultado,
        CarsItemsResponseDTO carsItems,
        // Vineland
        Integer vinelandComunicacion,
        Integer vinelandAutovalimiento,
        Integer vinelandSocial,
        Integer vinelandMotor,
        Integer vinelandCocienteFinal,
        Integer vinelandConductaDesadaptativa,
        Integer vinelandInternalizante,
        Integer vinelandExternalizante,
        // Segunda visita
        LocalDate fechaExtraccion,
        boolean activo,
        LocalDateTime createdAt
) {}
