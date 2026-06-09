package com.utn.magtea.paciente;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;


@Mapper(componentModel = "spring")
public interface PacienteMapper {

    default MchatEstado calcularMchatEstado(Paciente entity) {
        if (entity.getMchatScoreTotal() != null) return MchatEstado.COMPLETADO;
        if (entity.getMchatToken() != null && entity.getMchatTokenExpiry() != null
                && entity.getMchatTokenExpiry().isAfter(LocalDateTime.now())) return MchatEstado.PENDIENTE;
        if (entity.getMchatToken() != null) return MchatEstado.EXPIRADO;
        return MchatEstado.NO_ENVIADO;
    }

    @Mapping(target = "edadActual",     expression = "java(entity.getFechaNacimientoNino() != null ? java.time.Period.between(entity.getFechaNacimientoNino(), java.time.LocalDate.now()).getYears() : null)")
    @Mapping(target = "pacienteEstado", source = "estadoClinico")
    @Mapping(target = "mchatEstado",    expression = "java(calcularMchatEstado(entity))")
    @Mapping(target = "mchatResultado", expression = "java(entity.getMchatScoreTotal() == null ? null : entity.getMchatScoreTotal() <= 2 ? com.utn.magtea.paciente.MchatRiesgo.BAJO_RIESGO : entity.getMchatScoreTotal() <= 7 ? com.utn.magtea.paciente.MchatRiesgo.MEDIANO_RIESGO : com.utn.magtea.paciente.MchatRiesgo.ALTO_RIESGO)")
    @Mapping(target = "carsResultado",  expression = "java(entity.getCarsRawScore() == null ? null : entity.getCarsRawScore() < 30.0 ? com.utn.magtea.paciente.CarsResultado.MINIMO_NO_TEA : entity.getCarsRawScore() < 37.0 ? com.utn.magtea.paciente.CarsResultado.LEVE_MODERADO : com.utn.magtea.paciente.CarsResultado.SEVERO)")
    PacienteResponseDTO toDTO(Paciente entity);

    @Mapping(target = "id",                          ignore = true)
    @Mapping(target = "formularioInteresId",         ignore = true)
    @Mapping(target = "codigoNumerico",              ignore = true)
    @Mapping(target = "fechaContacto",               ignore = true)
    @Mapping(target = "comoConocioProyecto",         ignore = true)
    @Mapping(target = "fechaPrimeraVisita",          ignore = true)
    @Mapping(target = "consentimientoFirmado",       ignore = true)
    @Mapping(target = "notas",                       ignore = true)
    @Mapping(target = "activo",                      ignore = true)
    @Mapping(target = "criteriosRegistrados",        ignore = true)
    @Mapping(target = "criterioTEADSMV",             ignore = true)
    @Mapping(target = "criterioTGDDSMIV",            ignore = true)
    @Mapping(target = "criterioEdad",                ignore = true)
    @Mapping(target = "epilepsia",                   ignore = true)
    @Mapping(target = "paralisisCerebral",           ignore = true)
    @Mapping(target = "infeccionesCongenitas",       ignore = true)
    @Mapping(target = "lesionesEstructuralesSNC",    ignore = true)
    @Mapping(target = "facomatosis",                 ignore = true)
    @Mapping(target = "patologiasNeurometabolicas",  ignore = true)
    @Mapping(target = "lesionesOcupantesEspacioSNC", ignore = true)
    @Mapping(target = "patologiaPsiquiatrica",       ignore = true)
    @Mapping(target = "otrosSindromesGeneticos",     ignore = true)
    @Mapping(target = "pubertadPrecoz",              ignore = true)
    @Mapping(target = "mchatScoreTotal",             ignore = true)
    @Mapping(target = "mchatSeguimientoFallas",      ignore = true)
    @Mapping(target = "mchatResultadoFinal",         ignore = true)
    @Mapping(target = "seguimientoItem1",            ignore = true)
    @Mapping(target = "seguimientoItem2",            ignore = true)
    @Mapping(target = "seguimientoItem3",            ignore = true)
    @Mapping(target = "seguimientoItem4",            ignore = true)
    @Mapping(target = "seguimientoItem5",            ignore = true)
    @Mapping(target = "seguimientoItem6",            ignore = true)
    @Mapping(target = "seguimientoItem7",            ignore = true)
    @Mapping(target = "seguimientoItem8",            ignore = true)
    @Mapping(target = "seguimientoItem9",            ignore = true)
    @Mapping(target = "seguimientoItem10",           ignore = true)
    @Mapping(target = "seguimientoItem11",           ignore = true)
    @Mapping(target = "seguimientoItem12",           ignore = true)
    @Mapping(target = "seguimientoItem13",           ignore = true)
    @Mapping(target = "seguimientoItem14",           ignore = true)
    @Mapping(target = "seguimientoItem15",           ignore = true)
    @Mapping(target = "seguimientoItem16",           ignore = true)
    @Mapping(target = "seguimientoItem17",           ignore = true)
    @Mapping(target = "seguimientoItem18",           ignore = true)
    @Mapping(target = "seguimientoItem19",           ignore = true)
    @Mapping(target = "seguimientoItem20",           ignore = true)
    @Mapping(target = "carsRawScore",                ignore = true)
    @Mapping(target = "carsTScore",                  ignore = true)
    @Mapping(target = "vinelandComunicacion",        ignore = true)
    @Mapping(target = "vinelandAutovalimiento",      ignore = true)
    @Mapping(target = "vinelandSocial",              ignore = true)
    @Mapping(target = "vinelandMotor",               ignore = true)
    @Mapping(target = "vinelandCocienteFinal",       ignore = true)
    @Mapping(target = "vinelandConductaDesadaptativa", ignore = true)
    @Mapping(target = "vinelandInternalizante",      ignore = true)
    @Mapping(target = "vinelandExternalizante",      ignore = true)
    @Mapping(target = "fechaExtraccion",             ignore = true)
    @Mapping(target = "mchatToken",                  ignore = true)
    @Mapping(target = "mchatTokenExpiry",            ignore = true)
    Paciente toEntity(PacienteCreateDTO dto);
}
