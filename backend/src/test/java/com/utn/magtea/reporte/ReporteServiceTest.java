package com.utn.magtea.reporte;

import com.utn.magtea.donacion.Donacion;
import com.utn.magtea.donacion.DonacionRepository;
import com.utn.magtea.donacion.EstadoDonacion;
import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.Sexo;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.reporte.dto.DashboardAnaliticaDTO;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private FormularioInteresRepository formularioRepository;

    @Mock
    private SueroRepository sueroRepository;

    @Mock
    private DonacionRepository donacionRepository;

    private final Clock clock = Clock.fixed(
        Instant.parse("2025-06-01T12:00:00Z"), ZoneId.of("America/Argentina/Cordoba"));

    private ReporteService service;

    @BeforeEach
    void setUp() {
        service = new ReporteService(
            pacienteRepository, formularioRepository, sueroRepository, donacionRepository, clock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarDashboardCompleto_cuandoFiltroTodos() {
        // Mock Paciente 1 (PROBLEMA, masculino, con escalas completas, extracción realizada)
        var paciente1 = new Paciente();
        paciente1.setId(1L);
        paciente1.setSexo(Sexo.MASCULINO);
        paciente1.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente1.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);

        var mchat1 = new MchatFamilia();
        mchat1.setScoreTotal(5);
        mchat1.setResultadoFinal(MchatResultadoFinal.POSITIVA);
        mchat1.setP1(true);
        mchat1.setP2(false);
        paciente1.setMchatFamilia(mchat1);

        // M-CHAT Seguimiento positivo (fallas >= 2)
        var seguimiento1 = new MchatSeguimiento();
        // Todos los campos booleanos quedan en false por defecto, lo que da 17 fallas (>= 2)
        paciente1.setMchatSeguimiento(seguimiento1);

        var cars1 = new EvaluacionCars();
        cars1.setRawScore(new BigDecimal("35.0"));
        paciente1.setEvaluacionCars(cars1);

        var vineland1 = new EvaluacionVineland();
        vineland1.setComunicacion(80);
        vineland1.setAutovalimiento(85);
        vineland1.setSocial(75);
        vineland1.setMotor(90);
        vineland1.setCocienteFinal(82);
        vineland1.setConductaDesadaptativa(15);
        vineland1.setInternalizante(10);
        vineland1.setExternalizante(12);
        paciente1.setEvaluacionVineland(vineland1);

        // Mock Paciente 2 (CONTROL, femenino, riesgo bajo, seguimiento negativo, admitido)
        var paciente2 = new Paciente();
        paciente2.setId(2L);
        paciente2.setSexo(Sexo.FEMENINO);
        paciente2.setTipoPaciente(TipoPaciente.CONTROL);
        paciente2.setEstadoClinico(PacienteEstado.ADMITIDO);

        var mchat2 = new MchatFamilia();
        mchat2.setScoreTotal(1);
        mchat2.setResultadoFinal(MchatResultadoFinal.NEGATIVA);
        paciente2.setMchatFamilia(mchat2);

        // M-CHAT Seguimiento negativo (fallas < 2)
        var seguimiento2 = new MchatSeguimiento();
        // Seteamos todos los items a true (pasa) excepto los invertidos para tener 0 fallas
        seguimiento2.setItem1(true);
        seguimiento2.setItem3(true);
        seguimiento2.setItem4(true);
        seguimiento2.setItem6(true);
        seguimiento2.setItem7(true);
        seguimiento2.setItem8(true);
        seguimiento2.setItem9(true);
        seguimiento2.setItem10(true);
        seguimiento2.setItem11(true);
        seguimiento2.setItem13(true);
        seguimiento2.setItem14(true);
        seguimiento2.setItem15(true);
        seguimiento2.setItem16(true);
        seguimiento2.setItem17(true);
        seguimiento2.setItem18(true);
        seguimiento2.setItem19(true);
        seguimiento2.setItem20(true);
        paciente2.setMchatSeguimiento(seguimiento2);

        var cars2 = new EvaluacionCars();
        cars2.setRawScore(new BigDecimal("20.0")); // No TEA (< 30)
        paciente2.setEvaluacionCars(cars2);

        var vineland2 = new EvaluacionVineland();
        vineland2.setComunicacion(100);
        vineland2.setAutovalimiento(100);
        vineland2.setSocial(100);
        vineland2.setMotor(100);
        vineland2.setCocienteFinal(100);
        paciente2.setEvaluacionVineland(vineland2);

        List<Paciente> pacientes = List.of(paciente1, paciente2);

        // Mock PacienteRepository
        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(pacientes);

        // Mock FormularioInteres list
        var form1 = new FormularioInteres();
        form1.setId(1L);
        form1.setEstado(EstadoFormulario.ADMITIDO);
        form1.setComoConocioProyecto(ComoConocioProyecto.INSTAGRAM);

        var form2 = new FormularioInteres();
        form2.setId(2L);
        form2.setEstado(EstadoFormulario.CONTACTADO);
        form2.setComoConocioProyecto(ComoConocioProyecto.SUGERIDO_MEDICO);

        var form3 = new FormularioInteres();
        form3.setId(3L);
        form3.setEstado(EstadoFormulario.PENDIENTE);

        List<FormularioInteres> formularios = List.of(form1, form2, form3);

        // Mock FormularioInteresRepository
        when(formularioRepository.findAll(any(Specification.class))).thenReturn(formularios);

        // Mock SueroRepository (sin sueros registrados)
        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of());

        // Ejecutar
        DashboardAnaliticaDTO result = service.getDashboard();

        // Verificaciones
        assertThat(result).isNotNull();
        
        // Resumen
        assertThat(result.resumen().totalFormularios()).isEqualTo(3L);
        assertThat(result.resumen().formulariosContactados()).isEqualTo(2L); // 1 admitido + 1 contactado
        assertThat(result.resumen().formulariosAdmitidos()).isEqualTo(1L); // 1 admitido
        assertThat(result.resumen().pacientesTotal()).isEqualTo(2);
        assertThat(result.resumen().pacientesProblema()).isEqualTo(1);
        
        // Embudo (4 etapas en Opción E)
        assertThat(result.embudo().etapas()).hasSize(4);
        assertThat(result.embudo().etapas().get(0).nombre()).isEqualTo("Admitidos");
        assertThat(result.embudo().etapas().get(0).n()).isEqualTo(2);
        assertThat(result.embudo().etapas().get(1).nombre()).isEqualTo("Primera visita realizada");
        assertThat(result.embudo().etapas().get(1).n()).isEqualTo(1); // paciente 1 (EXTRACCION_REALIZADA)
        assertThat(result.embudo().etapas().get(2).nombre()).isEqualTo("Extracción pendiente");
        assertThat(result.embudo().etapas().get(2).n()).isEqualTo(1); // paciente 1
        assertThat(result.embudo().etapas().get(3).nombre()).isEqualTo("Extracción realizada");
        assertThat(result.embudo().etapas().get(3).n()).isEqualTo(1); // paciente 1

        // Demográfico
        assertThat(result.demografico().sexo()).hasSize(2);
        assertThat(result.demografico().fuenteDerivacion()).hasSize(2); // Instagram y Sugerido Médico

        // M-CHAT
        assertThat(result.mchat().totalConMchat()).isEqualTo(2);
        assertThat(result.mchat().riesgoMedio()).isEqualTo(1); // score 5
        assertThat(result.mchat().totalConSeguimiento()).isEqualTo(2);
        assertThat(result.mchat().riesgoMedioPositiva()).isEqualTo(1); // seguimiento 1 (17 fallas)
        assertThat(result.mchat().riesgoMedioNegativa()).isEqualTo(1); // seguimiento 2 (0 fallas)

        // CARS
        assertThat(result.cars().totalConCars()).isEqualTo(2);
        assertThat(result.cars().leveModerado()).isEqualTo(1); // 35.0
        assertThat(result.cars().minimoNoTea()).isEqualTo(1); // 20.0

        // Vineland
        assertThat(result.vineland().totalConVineland()).isEqualTo(2);
        assertThat(result.vineland().mediaCocienteFinal()).isEqualTo(91); // round((82 + 100) / 2)
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarCorrelaciones_cuandoEjesValidos() {
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setCodigoNumerico("P001");
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setFechaNacimientoNino(LocalDate.now().minusYears(3));

        var cars = new EvaluacionCars();
        cars.setRawScore(new BigDecimal("35.0"));
        paciente.setEvaluacionCars(cars);

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));

        var suero = new com.utn.magtea.suero.Suero();
        suero.setValorAnticuerpos(new BigDecimal("120.0"));
        suero.setPaciente(paciente);
        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of(suero));

        var resultado = service.getCorrelaciones("BTU_VALUE", "CARS_RAW");

        assertThat(resultado.puntos()).hasSize(1);
        assertThat(resultado.puntos().get(0).x()).isEqualTo(120.0);
        assertThat(resultado.puntos().get(0).y()).isEqualTo(35.0);
        assertThat(resultado.puntos().get(0).codigoNumerico()).isEqualTo("P001");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarListaVacia_cuandoPacientesSinValoresEnEjes() {
        // Paciente sin CARS ni suero → x y y serán null → no se incluye en puntos
        var paciente = new Paciente();
        paciente.setId(2L);
        paciente.setCodigoNumerico("P002");
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setFechaNacimientoNino(LocalDate.now().minusYears(2));

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));
        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of());

        var resultado = service.getCorrelaciones("BTU_VALUE", "CARS_RAW");

        assertThat(resultado.puntos()).isEmpty();
        assertThat(resultado.n()).isEqualTo(0);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoParNoIncluyeAnticuerpos() {
        assertThatThrownBy(() -> service.getCorrelaciones("MCHAT_SCORE", "CARS_RAW"))
                .isInstanceOf(com.utn.magtea.common.exception.BusinessRuleException.class)
                .hasMessageContaining("BTU_VALUE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarDashboard_cuandoNoHayPacientes() {
        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(formularioRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var result = service.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.resumen().pacientesTotal()).isEqualTo(0);
        assertThat(result.mchat().totalConMchat()).isEqualTo(0);
        assertThat(result.cars().totalConCars()).isEqualTo(0);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoEjeXNoValido() {
        assertThatThrownBy(() -> service.getCorrelaciones("EJE_INVALIDO", "CARS_RAW"))
                .isInstanceOf(com.utn.magtea.common.exception.BusinessRuleException.class)
                .hasMessageContaining("Eje no válido");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoEjeYNoValido() {
        assertThatThrownBy(() -> service.getCorrelaciones("MCHAT_SCORE", "EJE_INVALIDO"))
                .isInstanceOf(com.utn.magtea.common.exception.BusinessRuleException.class)
                .hasMessageContaining("Eje no válido");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarCorrelacionesBTU_cuandoHaySueros() {
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setCodigoNumerico("P001");
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setFechaNacimientoNino(LocalDate.now().minusYears(3));

        var mchat = new MchatFamilia();
        mchat.setScoreTotal(5);
        paciente.setMchatFamilia(mchat);

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));

        var suero = new com.utn.magtea.suero.Suero();
        suero.setValorAnticuerpos(new java.math.BigDecimal("120.0"));
        suero.setPaciente(paciente);

        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of(suero));

        var resultado = service.getCorrelaciones("BTU_VALUE", "MCHAT_SCORE");

        assertThat(resultado.puntos()).hasSize(1);
        assertThat(resultado.puntos().get(0).x()).isEqualTo(120.0);
        assertThat(resultado.puntos().get(0).y()).isEqualTo(5.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_usarScoreDeSeguimiento_cuandoCorrelacionMchatConSeguimiento() {
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setCodigoNumerico("P001");
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setFechaNacimientoNino(LocalDate.now().minusYears(3));

        var mchat = new MchatFamilia();
        mchat.setScoreTotal(5); // riesgo mediano → requirió seguimiento
        paciente.setMchatFamilia(mchat);

        var seguimiento = new MchatSeguimiento();
        seguimiento.setFallas(1); // seguimiento reevaluó a riesgo negativo
        paciente.setMchatSeguimiento(seguimiento);

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));

        var suero = new com.utn.magtea.suero.Suero();
        suero.setValorAnticuerpos(new java.math.BigDecimal("120.0"));
        suero.setPaciente(paciente);

        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of(suero));

        var resultado = service.getCorrelaciones("BTU_VALUE", "MCHAT_SCORE");

        assertThat(resultado.puntos()).hasSize(1);
        assertThat(resultado.puntos().get(0).y()).isEqualTo(1.0); // usa fallas del seguimiento, no scoreTotal
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_calcularAnticuerpos_cuandoHaySueros() {
        var paciente1 = new Paciente();
        paciente1.setId(1L);
        paciente1.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente1.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);

        var paciente2 = new Paciente();
        paciente2.setId(2L);
        paciente2.setTipoPaciente(TipoPaciente.CONTROL);
        paciente2.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente1, paciente2));
        when(formularioRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var suero1 = new com.utn.magtea.suero.Suero();
        suero1.setValorAnticuerpos(new java.math.BigDecimal("100.0"));
        suero1.setRango(2);
        suero1.setPaciente(paciente1);

        var suero2 = new com.utn.magtea.suero.Suero();
        suero2.setValorAnticuerpos(new java.math.BigDecimal("200.0"));
        suero2.setRango(3);
        suero2.setPaciente(paciente2);

        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of(suero1, suero2));

        var result = service.getDashboard();

        var distribucion = result.anticuerpos().distribucionRangos();
        assertThat(distribucion.get(2).n()).isEqualTo(1);
        assertThat(distribucion.get(2).porcentaje()).isEqualTo(50.0);
        assertThat(distribucion.get(3).n()).isEqualTo(1);
        assertThat(distribucion.get(3).porcentaje()).isEqualTo(50.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarVineland_cuandoSinCamposOpcionales() {
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);

        var vineland = new EvaluacionVineland();
        vineland.setComunicacion(80);
        vineland.setAutovalimiento(75);
        vineland.setSocial(70);
        vineland.setMotor(85);
        vineland.setCocienteFinal(78);
        // conductaDesadaptativa, internalizante, externalizante → null

        paciente.setEvaluacionVineland(vineland);

        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));
        when(formularioRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of());

        var result = service.getDashboard();

        assertThat(result.vineland().mediaCocienteFinal()).isEqualTo(78.0);
        assertThat(result.vineland().mediaConductaDesadaptativa()).isNull();
        assertThat(result.vineland().mediaInternalizante()).isNull();
        assertThat(result.vineland().mediaExternalizante()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_retornarRNulo_cuandoMenosDeDosCorrelaciones() {
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setCodigoNumerico("P001");
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);
        paciente.setFechaNacimientoNino(LocalDate.now().minusYears(3));

        var mchat = new MchatFamilia();
        mchat.setScoreTotal(5);
        paciente.setMchatFamilia(mchat);

        // Solo 1 punto → Pearson retorna null
        when(pacienteRepository.findAll(any(Specification.class))).thenReturn(List.of(paciente));

        var suero = new com.utn.magtea.suero.Suero();
        suero.setValorAnticuerpos(new java.math.BigDecimal("120.0"));
        suero.setPaciente(paciente);
        when(sueroRepository.findAllByPacienteIdInAndActivoTrue(any())).thenReturn(List.of(suero));

        var resultado = service.getCorrelaciones("BTU_VALUE", "MCHAT_SCORE");

        assertThat(resultado.n()).isEqualTo(1);
        assertThat(resultado.r()).isNull();
    }

    @Test
    void deberia_calcularDonaciones_cuandoHayDonacionesEnDistintosEstados() {
        var aprobada1 = new Donacion();
        aprobada1.setMonto(5000L);
        aprobada1.setEstado(EstadoDonacion.APROBADO);
        ReflectionTestUtils.setField(aprobada1, "createdAt", LocalDateTime.of(2025, 5, 10, 10, 0));

        var aprobada2 = new Donacion();
        aprobada2.setMonto(3000L);
        aprobada2.setEstado(EstadoDonacion.APROBADO);
        ReflectionTestUtils.setField(aprobada2, "createdAt", LocalDateTime.of(2025, 5, 20, 10, 0));

        var aprobada3 = new Donacion();
        aprobada3.setMonto(2000L);
        aprobada3.setEstado(EstadoDonacion.APROBADO);
        ReflectionTestUtils.setField(aprobada3, "createdAt", LocalDateTime.of(2025, 6, 1, 10, 0));

        var pendiente = new Donacion();
        pendiente.setMonto(1000L);
        pendiente.setEstado(EstadoDonacion.PENDIENTE);
        ReflectionTestUtils.setField(pendiente, "createdAt", LocalDateTime.of(2025, 6, 1, 10, 0));

        when(donacionRepository.findAll())
            .thenReturn(List.of(aprobada1, aprobada2, aprobada3, pendiente));

        var resultado = service.getDonaciones();

        assertThat(resultado.totalRecaudado()).isEqualTo(10000L);
        assertThat(resultado.cantidadAprobadas()).isEqualTo(3);
        assertThat(resultado.montoPromedio()).isCloseTo(3333.33, within(0.01));

        assertThat(resultado.recaudacionPorMes()).hasSize(2);
        assertThat(resultado.recaudacionPorMes().get(0).periodo()).isEqualTo("2025-05");
        assertThat(resultado.recaudacionPorMes().get(0).monto()).isEqualTo(8000L);
        assertThat(resultado.recaudacionPorMes().get(1).periodo()).isEqualTo("2025-06");
        assertThat(resultado.recaudacionPorMes().get(1).monto()).isEqualTo(2000L);

        assertThat(resultado.porEstado()).hasSize(EstadoDonacion.values().length);
        assertThat(resultado.porEstado().stream()
            .filter(d -> d.label().equals("APROBADO")).findFirst().orElseThrow().n()).isEqualTo(3);
        assertThat(resultado.porEstado().stream()
            .filter(d -> d.label().equals("PENDIENTE")).findFirst().orElseThrow().n()).isEqualTo(1);
    }

    @Test
    void deberia_retornarCeros_cuandoNoHayDonaciones() {
        when(donacionRepository.findAll()).thenReturn(List.of());

        var resultado = service.getDonaciones();

        assertThat(resultado.totalRecaudado()).isZero();
        assertThat(resultado.cantidadAprobadas()).isZero();
        assertThat(resultado.montoPromedio()).isZero();
        assertThat(resultado.recaudacionPorMes()).isEmpty();
        assertThat(resultado.porEstado()).allSatisfy(d -> assertThat(d.n()).isZero());
    }

    @Test
    void deberia_listarDonantes_soloAprobadosConAlMenosUnDatoDeContacto() {
        var conAmbos = new Donacion();
        conAmbos.setMonto(1000L);
        conAmbos.setEstado(EstadoDonacion.APROBADO);
        conAmbos.setDonante("Ana Pérez");
        conAmbos.setCorreo("ana@mail.com");
        ReflectionTestUtils.setField(conAmbos, "createdAt", LocalDateTime.of(2025, 5, 10, 10, 0));

        var soloCorreo = new Donacion();
        soloCorreo.setMonto(500L);
        soloCorreo.setEstado(EstadoDonacion.APROBADO);
        soloCorreo.setCorreo("anonimo@mail.com");
        ReflectionTestUtils.setField(soloCorreo, "createdAt", LocalDateTime.of(2025, 5, 11, 10, 0));

        var sinDatos = new Donacion();
        sinDatos.setMonto(200L);
        sinDatos.setEstado(EstadoDonacion.APROBADO);
        ReflectionTestUtils.setField(sinDatos, "createdAt", LocalDateTime.of(2025, 5, 12, 10, 0));

        var pendienteConDatos = new Donacion();
        pendienteConDatos.setMonto(300L);
        pendienteConDatos.setEstado(EstadoDonacion.PENDIENTE);
        pendienteConDatos.setDonante("No Confirmado");
        pendienteConDatos.setCorreo("noconfirmado@mail.com");
        ReflectionTestUtils.setField(pendienteConDatos, "createdAt", LocalDateTime.of(2025, 5, 13, 10, 0));

        when(donacionRepository.findAll())
            .thenReturn(List.of(conAmbos, soloCorreo, sinDatos, pendienteConDatos));

        var resultado = service.getDonaciones();

        assertThat(resultado.donantes()).hasSize(2);
        assertThat(resultado.donantes()).extracting("donante", "correo")
            .containsExactlyInAnyOrder(
                tuple("Ana Pérez", "ana@mail.com"),
                tuple(null, "anonimo@mail.com")
            );
    }
}

