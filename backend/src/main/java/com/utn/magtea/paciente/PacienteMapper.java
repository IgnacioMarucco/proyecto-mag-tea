package com.utn.magtea.paciente;

import com.utn.magtea.paciente.cars.CarsItemsResponseDTO;
import com.utn.magtea.paciente.cars.CarsResultado;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.criterios.Criterios;
import com.utn.magtea.paciente.criterios.CriteriosAptitud;
import com.utn.magtea.paciente.mchat.MchatEstado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Mapper(componentModel = "spring")
public interface PacienteMapper {

    default CriteriosAptitud calcularCriteriosAptitud(Paciente entity) {
        Criterios c = entity.getCriterios();
        if (c == null) return null;
        boolean exclusion = c.isEpilepsia() || c.isParalisisCerebral() ||
                c.isInfeccionesCongenitas() || c.isLesionesEstructuralesSNC() ||
                c.isFacomatosis() || c.isPatologiasNeurometabolicas() ||
                c.isLesionesOcupantesEspacioSNC() || c.isPatologiaPsiquiatrica() ||
                c.isOtrosSindromesGeneticos() || c.isPubertadPrecoz();
        if (exclusion) return CriteriosAptitud.EXCLUIDO;
        if (entity.getTipoPaciente() == TipoPaciente.CONTROL) {
            // CONTROL: sin diagnóstico TEA/TGD, con criterio de edad
            if (!c.isCriterioTEADSMV() && !c.isCriterioTGDDSMIV() && c.isCriterioEdad())
                return CriteriosAptitud.APTO;
        } else {
            // PROBLEMA (o null como fallback): con diagnóstico TEA y TGD, con criterio de edad
            if (c.isCriterioTEADSMV() && c.isCriterioTGDDSMIV() && c.isCriterioEdad())
                return CriteriosAptitud.APTO;
        }
        return CriteriosAptitud.INCOMPLETO;
    }

    default MchatEstado calcularMchatEstado(Paciente entity) {
        if (entity.getMchatFamilia() != null) return MchatEstado.COMPLETADO;
        if (entity.getMchatToken() != null && entity.getMchatTokenExpiry() != null
                && entity.getMchatTokenExpiry().isAfter(LocalDateTime.now())) return MchatEstado.PENDIENTE;
        if (entity.getMchatToken() != null) return MchatEstado.EXPIRADO;
        return MchatEstado.NO_ENVIADO;
    }

    default CarsItemsResponseDTO toCarsItems(EvaluacionCars c) {
        return new CarsItemsResponseDTO(
                c.getItem1(),  c.getItem2(),  c.getItem3(),  c.getItem4(),  c.getItem5(),
                c.getItem6(),  c.getItem7(),  c.getItem8(),  c.getItem9(),  c.getItem10(),
                c.getItem11(), c.getItem12(), c.getItem13(), c.getItem14(), c.getItem15(),
                c.getObs1(),  c.getObs2(),  c.getObs3(),  c.getObs4(),  c.getObs5(),
                c.getObs6(),  c.getObs7(),  c.getObs8(),  c.getObs9(),  c.getObs10(),
                c.getObs11(), c.getObs12(), c.getObs13(), c.getObs14(), c.getObs15()
        );
    }

    default CarsResultado calcularCarsResultado(Paciente entity) {
        if (entity.getEvaluacionCars() == null || entity.getEvaluacionCars().getRawScore() == null) return null;
        BigDecimal raw = entity.getEvaluacionCars().getRawScore();
        if (raw.compareTo(BigDecimal.valueOf(30)) < 0) return CarsResultado.MINIMO_NO_TEA;
        if (raw.compareTo(BigDecimal.valueOf(37)) < 0) return CarsResultado.LEVE_MODERADO;
        return CarsResultado.SEVERO;
    }

    @Mapping(target = "edadActual",     expression = "java(entity.getFechaNacimientoNino() != null ? java.time.Period.between(entity.getFechaNacimientoNino(), java.time.LocalDate.now()).getYears()  : null)")
    @Mapping(target = "edadMeses",      expression = "java(entity.getFechaNacimientoNino() != null ? java.time.Period.between(entity.getFechaNacimientoNino(), java.time.LocalDate.now()).getMonths() : null)")
    @Mapping(target = "pacienteEstado", source = "estadoClinico")
    @Mapping(target = "criteriosAptitud", expression = "java(calcularCriteriosAptitud(entity))")
    @Mapping(target = "mchatEstado",    expression = "java(calcularMchatEstado(entity))")
    @Mapping(target = "mchatScoreTotal",    expression = "java(entity.getMchatFamilia() != null ? entity.getMchatFamilia().getScoreTotal() : null)")
    @Mapping(target = "mchatResultadoFinal",expression = "java(entity.getMchatFamilia() != null ? entity.getMchatFamilia().getResultadoFinal() : null)")
    @Mapping(target = "mchatResultado", expression = "java(entity.getMchatFamilia() == null ? null : entity.getMchatFamilia().getScoreTotal() <= 2 ? com.utn.magtea.paciente.mchat.MchatRiesgo.BAJO_RIESGO : entity.getMchatFamilia().getScoreTotal() <= 7 ? com.utn.magtea.paciente.mchat.MchatRiesgo.MEDIANO_RIESGO : com.utn.magtea.paciente.mchat.MchatRiesgo.ALTO_RIESGO)")
    @Mapping(target = "carsResultado",  expression = "java(calcularCarsResultado(entity))")
    // Criterios (desde sub-entidad)
    @Mapping(target = "criteriosRegistrados",          expression = "java(entity.getCriterios() != null)")
    @Mapping(target = "criterioTEADSMV",               expression = "java(entity.getCriterios() != null && entity.getCriterios().isCriterioTEADSMV())")
    @Mapping(target = "criterioTGDDSMIV",              expression = "java(entity.getCriterios() != null && entity.getCriterios().isCriterioTGDDSMIV())")
    @Mapping(target = "criterioEdad",                  expression = "java(entity.getCriterios() != null && entity.getCriterios().isCriterioEdad())")
    @Mapping(target = "epilepsia",                     expression = "java(entity.getCriterios() != null && entity.getCriterios().isEpilepsia())")
    @Mapping(target = "paralisisCerebral",             expression = "java(entity.getCriterios() != null && entity.getCriterios().isParalisisCerebral())")
    @Mapping(target = "infeccionesCongenitas",         expression = "java(entity.getCriterios() != null && entity.getCriterios().isInfeccionesCongenitas())")
    @Mapping(target = "lesionesEstructuralesSNC",      expression = "java(entity.getCriterios() != null && entity.getCriterios().isLesionesEstructuralesSNC())")
    @Mapping(target = "facomatosis",                   expression = "java(entity.getCriterios() != null && entity.getCriterios().isFacomatosis())")
    @Mapping(target = "patologiasNeurometabolicas",    expression = "java(entity.getCriterios() != null && entity.getCriterios().isPatologiasNeurometabolicas())")
    @Mapping(target = "lesionesOcupantesEspacioSNC",   expression = "java(entity.getCriterios() != null && entity.getCriterios().isLesionesOcupantesEspacioSNC())")
    @Mapping(target = "patologiaPsiquiatrica",         expression = "java(entity.getCriterios() != null && entity.getCriterios().isPatologiaPsiquiatrica())")
    @Mapping(target = "otrosSindromesGeneticos",       expression = "java(entity.getCriterios() != null && entity.getCriterios().isOtrosSindromesGeneticos())")
    @Mapping(target = "pubertadPrecoz",                expression = "java(entity.getCriterios() != null && entity.getCriterios().isPubertadPrecoz())")
    // Seguimiento M-CHAT (desde sub-entidad)
    @Mapping(target = "mchatSeguimientoFallas", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().getFallas() : null)")
    @Mapping(target = "seguimientoItem1",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem1()  : null)")
    @Mapping(target = "seguimientoItem2",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem2()  : null)")
    @Mapping(target = "seguimientoItem3",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem3()  : null)")
    @Mapping(target = "seguimientoItem4",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem4()  : null)")
    @Mapping(target = "seguimientoItem5",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem5()  : null)")
    @Mapping(target = "seguimientoItem6",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem6()  : null)")
    @Mapping(target = "seguimientoItem7",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem7()  : null)")
    @Mapping(target = "seguimientoItem8",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem8()  : null)")
    @Mapping(target = "seguimientoItem9",  expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem9()  : null)")
    @Mapping(target = "seguimientoItem10", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem10() : null)")
    @Mapping(target = "seguimientoItem11", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem11() : null)")
    @Mapping(target = "seguimientoItem12", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem12() : null)")
    @Mapping(target = "seguimientoItem13", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem13() : null)")
    @Mapping(target = "seguimientoItem14", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem14() : null)")
    @Mapping(target = "seguimientoItem15", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem15() : null)")
    @Mapping(target = "seguimientoItem16", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem16() : null)")
    @Mapping(target = "seguimientoItem17", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem17() : null)")
    @Mapping(target = "seguimientoItem18", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem18() : null)")
    @Mapping(target = "seguimientoItem19", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem19() : null)")
    @Mapping(target = "seguimientoItem20", expression = "java(entity.getMchatSeguimiento() != null ? entity.getMchatSeguimiento().isItem20() : null)")
    // CARS-2 (desde sub-entidad)
    @Mapping(target = "carsRawScore",  expression = "java(entity.getEvaluacionCars() != null ? entity.getEvaluacionCars().getRawScore()  : null)")
    @Mapping(target = "carsTScore",    expression = "java(entity.getEvaluacionCars() != null ? entity.getEvaluacionCars().getTScore()    : null)")
    @Mapping(target = "carsPercentil", expression = "java(entity.getEvaluacionCars() != null ? entity.getEvaluacionCars().getPercentil() : null)")
    @Mapping(target = "carsItems",     expression = "java(entity.getEvaluacionCars() != null ? toCarsItems(entity.getEvaluacionCars())   : null)")
    // Vineland (desde sub-entidad)
    @Mapping(target = "vinelandComunicacion",          expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getComunicacion()          : null)")
    @Mapping(target = "vinelandAutovalimiento",        expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getAutovalimiento()        : null)")
    @Mapping(target = "vinelandSocial",                expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getSocial()                : null)")
    @Mapping(target = "vinelandMotor",                 expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getMotor()                 : null)")
    @Mapping(target = "vinelandCocienteFinal",         expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getCocienteFinal()         : null)")
    @Mapping(target = "vinelandConductaDesadaptativa", expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getConductaDesadaptativa() : null)")
    @Mapping(target = "vinelandInternalizante",        expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getInternalizante()        : null)")
    @Mapping(target = "vinelandExternalizante",        expression = "java(entity.getEvaluacionVineland() != null ? entity.getEvaluacionVineland().getExternalizante()        : null)")
    PacienteResponseDTO toDTO(Paciente entity);

    @Mapping(target = "pacienteEstado", source = "estadoClinico")
    PacienteListDTO toListDTO(Paciente entity);

    @Mapping(target = "id",                   ignore = true)
    @Mapping(target = "estadoClinico",         ignore = true)
    @Mapping(target = "formularioInteresId",   ignore = true)
    @Mapping(target = "codigoNumerico",        ignore = true)
    @Mapping(target = "fechaContacto",         ignore = true)
    @Mapping(target = "comoConocioProyecto",   ignore = true)
    @Mapping(target = "otroComoConocio",       ignore = true)
    @Mapping(target = "fechaPrimeraVisita",    ignore = true)
    @Mapping(target = "consentimientoFirmado", ignore = true)
    @Mapping(target = "notas",                 ignore = true)
    @Mapping(target = "activo",                ignore = true)
    @Mapping(target = "mchatFamilia",          ignore = true)
    @Mapping(target = "mchatToken",            ignore = true)
    @Mapping(target = "mchatTokenExpiry",      ignore = true)
    @Mapping(target = "fechaExtraccion",       ignore = true)
    @Mapping(target = "criterios",             ignore = true)
    @Mapping(target = "mchatSeguimiento",      ignore = true)
    @Mapping(target = "evaluacionCars",        ignore = true)
    @Mapping(target = "evaluacionVineland",    ignore = true)
    Paciente toEntity(PacienteCreateDTO dto);
}
