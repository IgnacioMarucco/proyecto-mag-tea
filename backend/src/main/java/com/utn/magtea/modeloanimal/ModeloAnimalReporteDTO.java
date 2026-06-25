package com.utn.magtea.modeloanimal;

import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.CarsItemsResponseDTO;
import com.utn.magtea.paciente.cars.CarsResultado;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatRiesgo;
import com.utn.magtea.suero.SueroUso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ModeloAnimalReporteDTO(
        Long id,
        String identificador,
        SexoRaton sexo,
        String camadaNombre,
        LocalDate fechaNacimiento,
        LocalDate fechaDia1Inoculacion,
        List<InoculacionReporteDTO> inoculaciones,
        PoolReporteDTO pool,
        List<SueroReporteDTO> sueros,
        VocalizacionesDTO vocalizaciones,
        TresCamarasDTO tresCamaras,
        Integer numCelulasGanglionares,
        Integer numCelulasPurkinje,
        LocalDateTime createdAt
) {

    public record InoculacionReporteDTO(
            Integer dia,
            LocalDate fecha,
            java.math.BigDecimal cantidadConsumida
    ) {}

    public record PoolReporteDTO(
            String codigo,
            int rango,
            SueroUso uso,
            LocalDate fechaCreacion,
            String freezer,
            Integer cajon,
            Integer numeroCaja
    ) {}

    public record SueroReporteDTO(
            BigDecimal valorAnticuerpos,
            Integer rango,
            LocalDate fechaExtraccion,
            PacienteReporteDTO paciente
    ) {}

    public record PacienteReporteDTO(
            String codigoNumerico,
            TipoPaciente tipoPaciente,
            java.time.LocalDateTime fechaPrimeraVisita,
            // M-CHAT Familia (null si no se realizó)
            List<Boolean> mchatFamiliaItems,
            Integer mchatScoreTotal,
            MchatRiesgo mchatRiesgo,
            MchatResultadoFinal mchatResultadoFinal,
            // M-CHAT Seguimiento (null si no se realizó — reemplaza a familia en el reporte)
            List<Boolean> mchatSeguimientoItems,
            Integer mchatSeguimientoFallas,
            // CARS-2
            CarsItemsResponseDTO carsItems,
            BigDecimal carsRawScore,
            BigDecimal carsTScore,
            Integer carsPercentil,
            CarsResultado carsResultado,
            // Vineland
            Integer vinelandComunicacion,
            Integer vinelandAutovalimiento,
            Integer vinelandSocial,
            Integer vinelandMotor,
            Integer vinelandCocienteFinal,
            Integer vinelandConductaDesadaptativa,
            Integer vinelandInternalizante,
            Integer vinelandExternalizante
    ) {}
}
