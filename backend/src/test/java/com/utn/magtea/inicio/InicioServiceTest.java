package com.utn.magtea.inicio;

import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.modeloanimal.ModeloAnimalPoolAporte;
import com.utn.magtea.modeloanimal.ModeloAnimalPoolAporteRepository;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.camada.Camada;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InicioServiceTest {

    // Reloj fijo al 2026-06-10 (miércoles), para que la semana comience el domingo 2026-06-07
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-10T12:00:00Z"), ZoneId.of("UTC"));

    @Mock private FormularioInteresRepository formularioRepo;
    @Mock private PacienteRepository pacienteRepo;
    @Mock private ModeloAnimalRepository modeloAnimalRepo;
    @Mock private ModeloAnimalPoolAporteRepository aporteRepo;
    @Mock private SueroRepository sueroRepo;
    @Mock private PoolRepository poolRepo;
    @Mock private ProfesionalRepository profesionalRepo;

    private InicioService service;

    @BeforeEach
    void setUp() {
        service = new InicioService(
                formularioRepo, pacienteRepo, modeloAnimalRepo,
                aporteRepo, sueroRepo, poolRepo, profesionalRepo, FIXED_CLOCK);
    }

    // ── getInicio — rol CUERPO_MEDICO ─────────────────────────────────────────────

    @Test
    void deberia_retornarFormulariosPendientes_cuandoRolEsCuerpoMedico() {
        when(formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE)).thenReturn(5L);
        when(pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_MEDICO");

        assertThat(result.formulariosPendientes()).isEqualTo(5);
        assertThat(result.agendaSemana()).isNotNull();
        assertThat(result.inoculacionesSemana()).isNull();
        assertThat(result.alertasConductuales()).isNull();
        assertThat(result.actividadReciente()).isNull();
    }

    @Test
    void deberia_incluirEventosPrimeraVisitaYExtraccion_cuandoRolCuerpoMedico() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var pacientePV = buildPaciente(1L, "P-0001");
        pacientePV.setFechaPrimeraVisita(hoy.atStartOfDay());

        var pacienteEx = buildPaciente(2L, "P-0002");
        pacienteEx.setFechaTurnoExtraccion(hoy.atStartOfDay());

        when(formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE)).thenReturn(0L);
        when(pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(any(), any()))
                .thenReturn(List.of(pacientePV));
        when(pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(any(), any()))
                .thenReturn(List.of(pacienteEx));

        var result = service.getInicio("CUERPO_MEDICO");

        assertThat(result.agendaSemana())
                .extracting(AgendaEventoDTO::categoria)
                .containsExactlyInAnyOrder("PRIMERA_VISITA", "EXTRACCION");
    }

    // ── getInicio — rol CUERPO_TECNICO ────────────────────────────────────────────

    @Test
    void deberia_retornarInoculacionesYAlertas_cuandoRolEsCuerpoTecnico() {
        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.formulariosPendientes()).isNull();
        assertThat(result.inoculacionesSemana()).isNotNull();
        assertThat(result.alertasConductuales()).isNotNull();
        assertThat(result.actividadReciente()).isNull();
    }

    @Test
    void deberia_incluirEventoVocalizaciones_cuandoModeloSinVocalizacionesEnSemana() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        // La camada nació hace DIA_VOCALIZACIONES=7 días → vocalizaciones caen HOY (en la semana)
        var camada = buildCamada(hoy.minusDays(7));
        var modelo = buildModelo(1L, "M-1", camada, null, null);

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any()))
                .thenReturn(List.of(modelo))
                .thenReturn(List.of()); // segunda llamada para tres-cámaras

        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.agendaSemana())
                .extracting(AgendaEventoDTO::categoria)
                .contains("VOCALIZACIONES");
    }

    @Test
    void deberia_noIncluirVocalizaciones_cuandoModeloYaTieneVocalizaciones() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var camada = buildCamada(hoy.minusDays(7));
        // Modelo con vocalizaciones ya registradas
        var modelo = buildModelo(2L, "M-2", camada, new VocalizacionesUltrasonicas(), null);

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any()))
                .thenReturn(List.of(modelo))
                .thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.agendaSemana())
                .extracting(AgendaEventoDTO::categoria)
                .doesNotContain("VOCALIZACIONES");
    }

    @Test
    void deberia_incluirEventosTresCamarasYMicroscopia_cuandoModeloSinTresCamaras() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var camada = buildCamada(hoy.minusDays(21));
        var modelo = buildModelo(3L, "M-3", camada, null, null);

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        // Primera llamada (vocalizaciones): lista vacía
        // Segunda llamada (tres-cámaras): el modelo
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any()))
                .thenReturn(List.of())
                .thenReturn(List.of(modelo));
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.agendaSemana())
                .extracting(AgendaEventoDTO::categoria)
                .containsExactlyInAnyOrder("TRES_CAMARAS", "MICROSCOPIA");
    }

    @Test
    void deberia_incluirInoculacionesPendientes_cuandoModeloEnCurso() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var modelo = buildModelo(4L, "M-4", buildCamada(hoy.minusDays(30)), null, null);
        modelo.setFechaDia1Inoculacion(hoy.minusDays(1)); // Día 1 fue ayer → Día 2 es hoy
        modelo.getAportes().clear(); // Sin aportes registrados

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of(modelo));
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.agendaSemana())
                .extracting(AgendaEventoDTO::categoria)
                .contains("INOCULACION");
    }

    @Test
    void deberia_generarAlertasConductuales_cuandoModelosCercanosDeDias() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        // Modelo cuya camada nació hace 5 días (DIA_VOCALIZACIONES=7 - VENTANA_ALERTA=2 → umbral = 5)
        var camada = buildCamada(hoy.minusDays(5));
        var modelo = buildModelo(5L, "M-5", camada, null, null);

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of(modelo));

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.alertasConductuales())
                .extracting(AlertaConductualItemDTO::tipoTest)
                .contains("VOCALIZACIONES");
    }

    // ── getInicio — rol INVESTIGADOR_PRINCIPAL ────────────────────────────────────

    @Test
    void deberia_retornarActividadReciente_cuandoRolEsInvestigadorPrincipal() {
        when(formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE)).thenReturn(0L);
        when(pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(pacienteRepo.findTop3ByActivoTrueAndEstadoClinicoOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(sueroRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(poolRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(modeloAnimalRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(aporteRepo.findTop3ForActividad()).thenReturn(List.of());
        when(profesionalRepo.findByEmailIn(any())).thenReturn(List.of());

        var result = service.getInicio("INVESTIGADOR_PRINCIPAL");

        assertThat(result.formulariosPendientes()).isNotNull();
        assertThat(result.inoculacionesSemana()).isNotNull();
        assertThat(result.alertasConductuales()).isNotNull();
        assertThat(result.actividadReciente()).isNotNull().isEmpty();
    }

    // ── getInicio — actividad reciente con datos ──────────────────────────────────

    @Test
    void deberia_retornarActividadConProfesional_cuandoHayRegistrosRecientes() throws Exception {
        var paciente = buildPaciente(10L, "P-001");
        setAuditableField(paciente, "createdAt", LocalDateTime.now(FIXED_CLOCK).minusHours(2));
        setAuditableField(paciente, "createdBy", "medico@test.com");

        var prof = new Profesional();
        prof.setNombre("Ana");
        prof.setApellido("García");
        prof.setEmail("medico@test.com");
        prof.setRole(Role.CUERPO_MEDICO);

        when(formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE)).thenReturn(0L);
        when(pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of(paciente));
        when(pacienteRepo.findTop3ByActivoTrueAndEstadoClinicoOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(sueroRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(poolRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(modeloAnimalRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(aporteRepo.findTop3ForActividad()).thenReturn(List.of());
        when(profesionalRepo.findByEmailIn(any())).thenReturn(List.of(prof));

        var result = service.getInicio("INVESTIGADOR_PRINCIPAL");

        assertThat(result.actividadReciente()).hasSize(1);
        assertThat(result.actividadReciente().get(0).tipo()).isEqualTo("PACIENTE");
        assertThat(result.actividadReciente().get(0).nombreProfesional()).isEqualTo("Ana García");
        assertThat(result.actividadReciente().get(0).rol()).isEqualTo("CUERPO_MEDICO");
    }

    @Test
    void deberia_retornarActividadSinNombre_cuandoProfesionalNoEncontrado() throws Exception {
        var paciente = buildPaciente(11L, "P-002");
        setAuditableField(paciente, "createdAt", LocalDateTime.now(FIXED_CLOCK).minusHours(1));
        setAuditableField(paciente, "createdBy", "desconocido@test.com");

        when(formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE)).thenReturn(0L);
        when(pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());
        when(pacienteRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of(paciente));
        when(pacienteRepo.findTop3ByActivoTrueAndEstadoClinicoOrderByUpdatedAtDesc(any())).thenReturn(List.of());
        when(sueroRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(poolRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(modeloAnimalRepo.findTop3ByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        when(aporteRepo.findTop3ForActividad()).thenReturn(List.of());
        when(profesionalRepo.findByEmailIn(any())).thenReturn(List.of()); // profesional no encontrado

        var result = service.getInicio("INVESTIGADOR_PRINCIPAL");

        assertThat(result.actividadReciente()).hasSize(1);
        assertThat(result.actividadReciente().get(0).nombreProfesional()).isNull();
    }

    // ── alertas conductuales — tres cámaras ──────────────────────────────────────

    @Test
    void deberia_generarAlertasTresCamarasYSacrificio_cuandoCamadaAntigua() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        // Camada nacida hace 21 días → umbralTresCamaras = hoy - (21-2) = hoy - 19 → fn (hoy-21) no isAfter(hoy-19)
        var camada = buildCamada(hoy.minusDays(21));
        // Vocalizaciones ya registradas, tresCamaras aún no
        var modelo = buildModelo(6L, "M-6", camada, new VocalizacionesUltrasonicas(), null);

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of(modelo));

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.alertasConductuales())
                .extracting(AlertaConductualItemDTO::tipoTest)
                .containsExactlyInAnyOrder("TRES_CAMARAS", "SACRIFICIO");
    }

    // ── agenda inoculación — días ya completados ──────────────────────────────────

    @Test
    void deberia_filtrarInoculaciones_cuandoTodosLosDiasFueraDeRango() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        // inicioSemana = 2026-06-07 (domingo). dia1 = hoy-7 = 2026-06-03 → días 1-4 = Jun 3-6, todos antes de Jun 7
        var modelo = buildModelo(7L, "M-7", buildCamada(hoy.minusDays(30)), null, null);
        modelo.setFechaDia1Inoculacion(hoy.minusDays(7));
        modelo.getAportes().clear();

        when(modeloAnimalRepo.findForAgendaInoculacion(any(), any())).thenReturn(List.of(modelo));
        when(modeloAnimalRepo.findByCamadaFechaNacimientoBetween(any(), any())).thenReturn(List.of());
        when(modeloAnimalRepo.findForAlertasConductuales(any(), any())).thenReturn(List.of());

        var result = service.getInicio("CUERPO_TECNICO");

        assertThat(result.inoculacionesSemana()).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private Paciente buildPaciente(Long id, String codigo) {
        var p = new Paciente();
        p.setId(id);
        p.setCodigoNumerico(codigo);
        p.setActivo(true);
        p.setEstadoClinico(PacienteEstado.ADMITIDO);
        return p;
    }

    private Camada buildCamada(LocalDate fechaNacimiento) {
        var c = new Camada();
        c.setNombre("C-TEST");
        c.setFechaNacimiento(fechaNacimiento);
        return c;
    }

    private ModeloAnimal buildModelo(Long id, String ident, Camada camada,
                                      VocalizacionesUltrasonicas vus, TresCamaras tresCamaras) {
        var m = new ModeloAnimal();
        m.setId(id);
        m.setIdentificador(ident);
        m.setActivo(true);
        m.setCamada(camada);
        m.setVocalizaciones(vus);
        m.setTresCamaras(tresCamaras);
        var pool = new Pool();
        pool.setCodigo("XYZ");
        m.setPool(pool);
        return m;
    }

    private static void setAuditableField(Object entity, String fieldName, Object value) throws Exception {
        Class<?> current = entity.getClass();
        while (current != null) {
            try {
                Field f = current.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(entity, value);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
