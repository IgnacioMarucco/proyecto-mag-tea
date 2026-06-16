package com.utn.magtea.paciente;

import com.utn.magtea.common.MailService;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresService;
import com.utn.magtea.paciente.cars.CarsDTO;
import com.utn.magtea.paciente.criterios.CriteriosDTO;
import com.utn.magtea.paciente.mchat.MchatEstado;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.mchat.MchatSeguimientoDTO;
import com.utn.magtea.paciente.vineland.VinelandDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(AHORA, ZoneId.of("UTC"));

    @Mock private PacienteRepository repository;
    @Mock private PacienteMapper mapper;
    @Mock private FormularioInteresService formularioService;
    @Mock private MailService mailService;
    @Mock private Clock clock;

    @InjectMocks private PacienteService service;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        lenient().when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
        ReflectionTestUtils.setField(service, "tokenExpiryDays", 30);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void deberia_crearPaciente_problema_enviaMchatYGeneraToken() {
        var dto = createDTO(TipoPaciente.PROBLEMA);
        var entidad = new Paciente();
        var response = buildResponse(1L);

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.existsByCodigoNumerico(anyString())).thenReturn(false);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isEqualTo(response);
        assertThat(entidad.getCodigoNumerico()).isNotBlank();
        assertThat(entidad.getMchatToken()).isNotBlank();
        assertThat(entidad.getMchatTokenExpiry()).isNotNull();
        assertThat(entidad.getEstadoClinico()).isEqualTo(PacienteEstado.ADMITIDO);
        verify(mailService).enviarLinkMchat(any(), any(), any(), any(), any());
    }

    @Test
    void deberia_crearPaciente_control_noEnviaMchatNiGeneraToken() {
        var dto = createDTO(TipoPaciente.CONTROL);
        var entidad = new Paciente();

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.existsByCodigoNumerico(anyString())).thenReturn(false);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.create(dto);

        assertThat(entidad.getMchatToken()).isNull();
        assertThat(entidad.getMchatTokenExpiry()).isNull();
        verify(mailService, never()).enviarLinkMchat(any(), any(), any(), any(), any());
    }

    @Test
    void deberia_crearPaciente_creacriteriosInline() {
        var dto = new PacienteCreateDTO(null, "García", "Ana", "ana@test.com", null,
                "Niño", "Nombre", null, Sexo.MASCULINO, LocalDateTime.now(), null,
                TipoPaciente.PROBLEMA,
                true, true, true,
                false, false, false, false, false, false, false, false, false, false,
                true);
        var entidad = new Paciente();

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.existsByCodigoNumerico(anyString())).thenReturn(false);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.create(dto);

        assertThat(entidad.getCriterios()).isNotNull();
        assertThat(entidad.getCriterios().isCriterioTEADSMV()).isTrue();
        assertThat(entidad.getCriterios().isCriterioEdad()).isTrue();
        assertThat(entidad.isConsentimientoFirmado()).isTrue();
    }

    @Test
    void deberia_crearPaciente_conFormulario_copiaYAdmite() {
        var dto = new PacienteCreateDTO(5L, "García", "Ana", "ana@test.com", null,
                "Niño", "Nombre", null, Sexo.MASCULINO, LocalDateTime.now(), null,
                TipoPaciente.PROBLEMA,
                true, true, true,
                false, false, false, false, false, false, false, false, false, false,
                false);
        var entidad = new Paciente();
        var formulario = formularioConDatos(5L);

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.existsByCodigoNumerico(anyString())).thenReturn(false);
        when(formularioService.admitir(5L)).thenReturn(formulario);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.create(dto);

        assertThat(entidad.getFormularioInteresId()).isEqualTo(5L);
        assertThat(entidad.getFechaContacto()).isEqualTo(formulario.getFechaContacto());
        assertThat(entidad.getComoConocioProyecto()).isEqualTo(ComoConocioProyecto.INSTAGRAM);
        verify(formularioService).admitir(5L);
    }

    @Test
    void deberia_crearPaciente_continuaAunqueFalleElMail() {
        var dto = createDTO(TipoPaciente.PROBLEMA);
        var entidad = new Paciente();

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.existsByCodigoNumerico(anyString())).thenReturn(false);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));
        doThrow(new RuntimeException("SMTP error")).when(mailService).enviarLinkMchat(any(), any(), any(), any(), any());

        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    void deberia_retornarPaciente_cuandoIdExiste() {
        var entidad = pacienteActivo(1L);
        var response = buildResponse(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(mapper.toDTO(entidad)).thenReturn(response);

        assertThat(service.findById(1L)).isEqualTo(response);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoPacienteInactivo() {
        var entidad = new Paciente();
        entidad.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    void deberia_retornarPacientes_cuandoListar() {
        var entidad = pacienteActivo(1L);
        var listDTO = buildListDTO(1L);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad)));
        when(mapper.toListDTO(entidad)).thenReturn(listDTO);

        var result = service.findAll(0, 20, null, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(1).contains(listDTO);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    // ─── updateCriterios ──────────────────────────────────────────────────────

    @Test
    void deberia_actualizarCriterios_yMarcarRegistrado() {
        var entidad = pacienteActivo(1L);
        var dto = new CriteriosDTO(true, true, true,
                false, false, false, false, false, false, false, false, false, false);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.updateCriterios(1L, dto);

        assertThat(entidad.getCriterios()).isNotNull();
        assertThat(entidad.getCriterios().isCriterioTEADSMV()).isTrue();
    }

    @Test
    void deberia_actualizarCriterios_conCriterioExclusion() {
        var entidad = pacienteActivo(1L);
        var dto = new CriteriosDTO(true, true, true,
                true, false, false, false, false, false, false, false, false, false);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.updateCriterios(1L, dto);

        assertThat(entidad.getCriterios()).isNotNull();
        assertThat(entidad.getCriterios().isEpilepsia()).isTrue();
    }

    // ─── updateMchatSeguimiento ───────────────────────────────────────────────

    @Test
    void deberia_actualizarSeguimiento_cuandoScoreEnRango() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(5);
        entidad.setMchatFamilia(familia);
        var dto = seguimientoConFallas(2);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.updateMchatSeguimiento(1L, dto);

        assertThat(entidad.getMchatSeguimiento()).isNotNull();
        assertThat(entidad.getMchatSeguimiento().getFallas()).isEqualTo(2);
        assertThat(familia.getResultadoFinal()).isEqualTo(MchatResultadoFinal.POSITIVA);
    }

    @Test
    void deberia_actualizarSeguimiento_cuandoUnaSolaFalla_resultadoNegativo() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(4);
        entidad.setMchatFamilia(familia);
        var dto = seguimientoConFallas(1);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.updateMchatSeguimiento(1L, dto);

        assertThat(familia.getResultadoFinal()).isEqualTo(MchatResultadoFinal.NEGATIVA);
    }

    @Test
    void deberia_lanzarExcepcion_alActualizarSeguimiento_cuandoSinFamiliaRegistrada() {
        var entidad = pacienteActivo(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.updateMchatSeguimiento(1L, seguimientoConFallas(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("seguimiento");
    }

    @Test
    void deberia_lanzarExcepcion_alActualizarSeguimiento_cuandoScoreFueraDeRango() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(8);
        entidad.setMchatFamilia(familia);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.updateMchatSeguimiento(1L, seguimientoConFallas(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3 y 7");
    }

    @Test
    void deberia_lanzarExcepcion_alActualizarSeguimiento_cuandoPacienteControl() {
        var entidad = pacienteActivo(1L);
        entidad.setTipoPaciente(TipoPaciente.CONTROL);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.updateMchatSeguimiento(1L, seguimientoConFallas(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("control");
    }

    // ─── updateCars ───────────────────────────────────────────────────────────

    @Test
    void deberia_actualizarCars_cuandoValoresValidos() {
        var entidad = pacienteActivo(1L);
        var dto = carsDTO(2.0);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L));

        service.updateCars(1L, dto);

        assertThat(entidad.getEvaluacionCars()).isNotNull();
        assertThat(entidad.getEvaluacionCars().getRawScore()).isEqualByComparingTo(new BigDecimal("30.0"));
    }

    @Test
    void deberia_lanzarExcepcion_alActualizarCars_cuandoValorInvalido() {
        var dto = new CarsDTO(
                new BigDecimal("1.0"), new BigDecimal("1.5"), new BigDecimal("2.0"),
                new BigDecimal("2.5"), new BigDecimal("3.0"), new BigDecimal("3.5"),
                new BigDecimal("4.0"), new BigDecimal("1.0"), new BigDecimal("1.5"),
                new BigDecimal("2.0"), new BigDecimal("2.5"), new BigDecimal("3.0"),
                new BigDecimal("3.5"), new BigDecimal("4.0"), new BigDecimal("5.0"),
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.updateCars(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5.0");
    }

    @Test
    void deberia_lanzarExcepcion_alActualizarCars_cuandoPacienteControl() {
        var entidad = pacienteActivo(1L);
        entidad.setTipoPaciente(TipoPaciente.CONTROL);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.updateCars(1L, carsDTO(2.0)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("control");
    }

    // ─── updateVineland ───────────────────────────────────────────────────────

    @Test
    void deberia_lanzarExcepcion_alActualizarVineland_cuandoPacienteControl() {
        var entidad = pacienteActivo(1L);
        entidad.setTipoPaciente(TipoPaciente.CONTROL);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.updateVineland(1L,
                new VinelandDTO(null, null, null, null, null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("control");
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void deberia_marcarInactivo_cuandoBajaLogica() {
        var entidad = pacienteActivo(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.delete(1L);

        assertThat(entidad.isActivo()).isFalse();
        verify(repository).save(entidad);
    }

    // ─── validarTokenMchat ────────────────────────────────────────────────────

    @Test
    void deberia_validarTokenMchat_cuandoTokenValido() {
        var entidad = pacienteActivo(1L);
        entidad.setMchatToken("token-abc");
        entidad.setMchatTokenExpiry(LocalDateTime.now(FIXED_CLOCK).plusDays(10));

        when(repository.findByMchatToken("token-abc")).thenReturn(Optional.of(entidad));

        var result = service.validarTokenMchat("token-abc");

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoTokenExpirado() {
        var entidad = pacienteActivo(1L);
        entidad.setMchatToken("token-viejo");
        entidad.setMchatTokenExpiry(LocalDateTime.now(FIXED_CLOCK).minusDays(1));

        when(repository.findByMchatToken("token-viejo")).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.validarTokenMchat("token-viejo"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoTokenNoExiste() {
        when(repository.findByMchatToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validarTokenMchat("token-inexistente"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── procesarMchatPublico ─────────────────────────────────────────────────

    @Test
    void deberia_procesarMchatPublico_limpiaToken_yActualizaEstado() {
        var entidad = pacienteActivo(1L);
        entidad.setMchatToken("token-abc");
        entidad.setMchatTokenExpiry(LocalDateTime.now(FIXED_CLOCK).plusDays(10));
        var familia = new MchatFamilia();
        familia.setScoreTotal(12);
        entidad.setMchatFamilia(familia);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.procesarMchatPublico(1L);

        assertThat(entidad.getMchatToken()).isNull();
        assertThat(entidad.getMchatTokenExpiry()).isNull();
        assertThat(entidad.getEstadoClinico()).isEqualTo(PacienteEstado.MCHAT_RESPONDIDO);
        verify(repository).save(entidad);
    }

    @Test
    void deberia_procesarMchatPublico_cuandoSinFamilia_quedaEnAdmitido() {
        var entidad = pacienteActivo(1L);
        entidad.setMchatToken("token-abc");

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.procesarMchatPublico(1L);

        assertThat(entidad.getMchatToken()).isNull();
        assertThat(entidad.getEstadoClinico()).isEqualTo(PacienteEstado.ADMITIDO);
    }

    // ─── actualizarMchatInterno ───────────────────────────────────────────────

    @Test
    void deberia_actualizarMchatInterno_cuandoScoreEnRango_noLimpiaSeguimiento() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(5);
        entidad.setMchatFamilia(familia);
        var seg = new MchatSeguimiento();
        seg.setItem1(true);
        seg.setFallas(1);
        entidad.setMchatSeguimiento(seg);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.actualizarMchatInterno(1L);

        assertThat(entidad.getMchatSeguimiento()).isNotNull();
        assertThat(entidad.getMchatSeguimiento().isItem1()).isTrue();
    }

    @Test
    void deberia_actualizarMchatInterno_cuandoScoreFueraDeRango_limpiaSeguimiento() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(1);
        entidad.setMchatFamilia(familia);
        var seg = new MchatSeguimiento();
        seg.setItem1(true);
        seg.setFallas(2);
        entidad.setMchatSeguimiento(seg);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.actualizarMchatInterno(1L);

        assertThat(entidad.getMchatSeguimiento()).isNull();
    }

    @Test
    void deberia_actualizarMchatInterno_cuandoScoreAlto_limpiaSeguimiento() {
        var entidad = pacienteActivo(1L);
        var familia = new MchatFamilia();
        familia.setScoreTotal(15);
        entidad.setMchatFamilia(familia);
        var seg = new MchatSeguimiento();
        seg.setItem1(true);
        entidad.setMchatSeguimiento(seg);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.actualizarMchatInterno(1L);

        assertThat(entidad.getMchatSeguimiento()).isNull();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Paciente pacienteActivo(Long id) {
        var p = new Paciente();
        p.setId(id);
        p.setActivo(true);
        p.setNombreNino("Nombre");
        p.setApellidoNino("Niño");
        p.setCorreoTutor("ana@test.com");
        p.setNombreTutor("Ana");
        p.setApellidoTutor("García");
        p.setFechaContacto(LocalDate.of(2026, 1, 1));
        p.setSexo(Sexo.MASCULINO);
        p.setTipoPaciente(TipoPaciente.PROBLEMA);
        p.setEstadoClinico(PacienteEstado.ADMITIDO);
        return p;
    }

    private PacienteCreateDTO createDTO(TipoPaciente tipo) {
        return new PacienteCreateDTO(null, "García", "Ana", "ana@test.com", null,
                "Niño", "Nombre", null, Sexo.MASCULINO, LocalDateTime.now(), null,
                tipo,
                false, false, false,
                false, false, false, false, false, false, false, false, false, false,
                false);
    }

    private FormularioInteres formularioConDatos(Long id) {
        var f = new FormularioInteres();
        f.setId(id);
        f.setActivo(true);
        f.setFechaContacto(LocalDate.of(2025, 6, 1));
        f.setComoConocioProyecto(ComoConocioProyecto.INSTAGRAM);
        return f;
    }

    // true=Pasa, false=Falla; ítems invertidos (2,5,12): true=Falla, false=Pasa
    // Usa ítems no invertidos 1,3,4,6... para acumular fallas (valor false)
    private MchatSeguimientoDTO seguimientoConFallas(int cantFallas) {
        boolean i1  = cantFallas < 1;  boolean i2  = false;
        boolean i3  = cantFallas < 2;  boolean i4  = cantFallas < 3;
        boolean i5  = false;           boolean i6  = cantFallas < 4;
        boolean i7  = cantFallas < 5;  boolean i8  = cantFallas < 6;
        boolean i9  = cantFallas < 7;  boolean i10 = cantFallas < 8;
        boolean i11 = cantFallas < 9;  boolean i12 = false;
        boolean i13 = cantFallas < 10; boolean i14 = cantFallas < 11;
        boolean i15 = cantFallas < 12; boolean i16 = cantFallas < 13;
        boolean i17 = cantFallas < 14; boolean i18 = cantFallas < 15;
        boolean i19 = cantFallas < 16; boolean i20 = cantFallas < 17;
        return new MchatSeguimientoDTO(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10,
                i11, i12, i13, i14, i15, i16, i17, i18, i19, i20);
    }

    private CarsDTO carsDTO(double valorPorItem) {
        BigDecimal v = BigDecimal.valueOf(valorPorItem);
        return new CarsDTO(v, v, v, v, v, v, v, v, v, v, v, v, v, v, v,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    private static PacienteListDTO buildListDTO(Long id) {
        return new PacienteListDTO(id, "García", "Ana", "Niño", "Nombre",
                null, TipoPaciente.PROBLEMA, PacienteEstado.ADMITIDO, null, null);
    }

    private static PacienteResponseDTO buildResponse(Long id) {
        return new PacienteResponseDTO(
                id, null, "TST00001", LocalDate.of(2026, 1, 1),
                "García", "Ana", "ana@test.com", null, "Niño", "Nombre",
                null, null, null, null, null, Sexo.MASCULINO, TipoPaciente.PROBLEMA, null,
                false, null, PacienteEstado.ADMITIDO, MchatEstado.NO_ENVIADO,
                false, null,
                false, false, false,
                false, false, false, false, false, false, false, false, false, false,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, true, null
        );
    }
}
