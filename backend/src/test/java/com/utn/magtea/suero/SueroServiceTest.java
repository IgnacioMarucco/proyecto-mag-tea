package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEvents;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.criterios.Criterios;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboInputDTO;
import com.utn.magtea.tubo.TuboRepository;
import com.utn.magtea.tubo.TuboService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SueroServiceTest {

    @Mock private SueroRepository repository;
    @Mock private SueroMapper mapper;
    @Mock private TuboRepository tuboRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ApplicationEventPublisher events;
    @Mock private CajaRepository cajaRepository;
    @Mock private TuboService tuboService;

    @InjectMocks private SueroService service;

    // --- findAll ---

    @Test
    void deberia_listarSueros_cuandoExisten() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var listDTO = buildListDTO(1L);
        var page = new PageImpl<>(List.of(suero));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toListDTO(suero)).thenReturn(listDTO);

        var result = service.findAll(0, 10, null, null, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void deberia_listarSueros_cuandoFiltroRangoYUso() {
        var page = new PageImpl<Suero>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, "PAC", List.of(1, 2), List.of(SueroUso.PROBLEMA), "P-0005", "fechaExtraccion", "asc");

        assertThat(result.content()).isEmpty();
    }

    @Test
    void deberia_listarSueros_cuandoFiltroUsosMultiples() {
        var page = new PageImpl<Suero>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, null,
                List.of(SueroUso.PROBLEMA, SueroUso.CONTROL), null, "createdAt", "desc");

        assertThat(result.content()).isEmpty();
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    // --- findById ---

    @Test
    void deberia_obtenerSuero_cuandoExiste() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var response = buildResponseDTO(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(mapper.toDTO(suero)).thenReturn(response);

        var result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoSueroNoExistePorId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con id 99 no existe");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoSueroInactivo() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        suero.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(suero));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con id 1 no existe");
    }

    // --- findByCodigoNumerico ---

    @Test
    void deberia_obtenerSuero_cuandoCodigoNumericoExiste() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var response = buildResponseDTO(1L);

        when(repository.findByPacienteCodigoNumericoAndActivoTrue("PAC-001")).thenReturn(Optional.of(suero));
        when(mapper.toDTO(suero)).thenReturn(response);

        var result = service.findByCodigoNumerico("PAC-001");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCodigoNumericoNoExiste() {
        when(repository.findByPacienteCodigoNumericoAndActivoTrue("INVALIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCodigoNumerico("INVALIDO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con código INVALIDO no existe");
    }

    // --- create ---

    @Test
    void deberia_crearSuero_cuandoDatosValidos() {
        var tubosInput = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0)), new TuboInputDTO("A2", BigDecimal.valueOf(1.0)));
        var dto = new SueroCreateDTO(1L, 1L, tubosInput, LocalDate.now(), new BigDecimal("1500"));

        var paciente = buildPaciente(1L, TipoPaciente.PROBLEMA);
        var caja = buildCaja(1L);
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var response = buildResponseDTO(1L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(repository.save(any(Suero.class))).thenReturn(suero);
        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(mapper.toDTO(suero)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(events).publishEvent(any(PacienteEvents.SueroRegistradoEvent.class));
        verify(repository).save(any(Suero.class));
    }

    @Test
    void deberia_actualizarEstadoAExtraccionRealizada_cuandoPacienteControl() {
        var tubosInput = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0)));
        var dto = new SueroCreateDTO(1L, 1L, tubosInput, LocalDate.now(), new BigDecimal("0"));

        var paciente = buildPaciente(1L, TipoPaciente.CONTROL);
        var caja = buildCaja(1L);
        var suero = buildSuero(1L, TipoPaciente.CONTROL);
        var response = buildResponseDTO(1L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(repository.save(any(Suero.class))).thenReturn(suero);
        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(mapper.toDTO(suero)).thenReturn(response);

        service.create(dto);

        verify(events).publishEvent(any(PacienteEvents.SueroRegistradoEvent.class));
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteYaTieneSuero() {
        var tubosInput = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0)));
        var dto = new SueroCreateDTO(1L, 1L, tubosInput, LocalDate.now(), new BigDecimal("100"));
        var paciente = buildPaciente(1L, TipoPaciente.PROBLEMA);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El paciente ya tiene un suero registrado");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPacienteNoExisteAlCrear() {
        var dto = new SueroCreateDTO(99L, 1L, List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0))), LocalDate.now(), new BigDecimal("100"));

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paciente con id 99 no existe");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteSinExtraccionPendiente() {
        var dto = new SueroCreateDTO(1L, 1L, List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0))), LocalDate.now(), new BigDecimal("100"));
        var paciente = buildPaciente(1L, TipoPaciente.PROBLEMA);
        paciente.setEstadoClinico(PacienteEstado.ADMITIDO);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("extracción pendiente");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteSinConsentimiento() {
        var dto = new SueroCreateDTO(1L, 1L, List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0))), LocalDate.now(), new BigDecimal("100"));
        var paciente = buildPaciente(1L, TipoPaciente.PROBLEMA);
        paciente.setConsentimientoFirmado(false);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("consentimiento");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteConCriterioExclusion() {
        // RN-04: si los criterios fueron actualizados post-admisión y resultan en EXCLUIDO,
        // el backend bloquea la creación del suero con HTTP 422
        var dto = new SueroCreateDTO(1L, 1L, List.of(new TuboInputDTO("A1", BigDecimal.valueOf(1.0))), LocalDate.now(), new BigDecimal("100"));
        var paciente = buildPaciente(1L, TipoPaciente.PROBLEMA);
        var criterios = new Criterios();
        criterios.setEpilepsia(true);
        paciente.setCriterios(criterios);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("exclusión");
    }

    // --- update ---

    @Test
    void deberia_actualizarSuero_cuandoDatosValidos() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var caja = buildCaja(1L);
        var tubosInput = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(2.0)));
        var dto = new SueroUpdateDTO(1L, tubosInput, LocalDate.now(), new BigDecimal("2000"));
        var response = buildResponseDTO(1L);

        var tuboExistente = new Tubo();
        tuboExistente.setPosicion("A1");
        tuboExistente.setCantidadInicial(BigDecimal.valueOf(1.0));
        tuboExistente.setCantidadUsada(BigDecimal.ZERO);

        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findBySueroId(1L)).thenReturn(List.of(tuboExistente));
        when(repository.save(suero)).thenReturn(suero);
        when(mapper.toDTO(suero)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result).isNotNull();
        verify(repository).save(suero);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoTuboConVolumenUsadoEliminado() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);
        var caja = buildCaja(1L);
        // dto pide solo A2, pero A1 tiene volumen usado → debe fallar
        var dto = new SueroUpdateDTO(1L, List.of(new TuboInputDTO("A2", BigDecimal.valueOf(1.0))), LocalDate.now(), new BigDecimal("1500"));

        var tuboConUso = new Tubo();
        tuboConUso.setPosicion("A1");
        tuboConUso.setCantidadInicial(BigDecimal.valueOf(1.0));
        tuboConUso.setCantidadUsada(BigDecimal.valueOf(0.3));

        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findBySueroId(1L)).thenReturn(List.of(tuboConUso));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("tiene")
                .hasMessageContaining("mL usados y no puede eliminarse");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoSueroNoExisteAlActualizar() {
        var dto = new SueroUpdateDTO(1L, List.of(), LocalDate.now(), new BigDecimal("1500"));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con id 99 no existe");
    }

    // --- delete ---

    @Test
    void deberia_eliminarSuero_cuandoExiste() {
        var suero = buildSuero(1L, TipoPaciente.PROBLEMA);

        when(repository.findById(1L)).thenReturn(Optional.of(suero));
        when(repository.save(suero)).thenReturn(suero);

        service.delete(1L);

        assertThat(suero.isActivo()).isFalse();
        verify(repository).save(argThat(s -> !s.isActivo()));
        verify(events).publishEvent(any(PacienteEvents.SueroEliminadoEvent.class));
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoSueroNoExisteAlEliminar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con id 99 no existe");
    }

    // --- getDisponibilidadPool ---

    @Test
    void deberia_calcularDisponibilidadPool_excluyendoSuerosAgotados() {
        var sueroAgotado = buildSuero(1L, TipoPaciente.PROBLEMA);
        sueroAgotado.setRango(1);

        var tuboAgotado = new Tubo();
        tuboAgotado.setId(1L);
        tuboAgotado.setSuero(sueroAgotado);
        tuboAgotado.setCantidadInicial(BigDecimal.valueOf(1.0));
        tuboAgotado.setCantidadUsada(BigDecimal.valueOf(1.0));

        var sueroDisponible = buildSuero(2L, TipoPaciente.PROBLEMA);
        sueroDisponible.setRango(1);

        var tuboDisponible = new Tubo();
        tuboDisponible.setId(2L);
        tuboDisponible.setSuero(sueroDisponible);
        tuboDisponible.setCantidadInicial(BigDecimal.valueOf(1.0));
        tuboDisponible.setCantidadUsada(BigDecimal.valueOf(0.5));

        when(tuboRepository.findBySueroActivoTrue()).thenReturn(List.of(tuboAgotado, tuboDisponible));

        var result = service.getDisponibilidadPool();

        assertThat(result).hasSize(1);
        var disp = result.getFirst();
        assertThat(disp.rango()).isEqualTo(1);
        assertThat(disp.cantidadSueros()).isEqualTo(1L);
        assertThat(disp.mlDisponibles()).isEqualByComparingTo("0.5");
        assertThat(disp.ratonesPosibles()).isEqualTo(2);
    }

    @Test
    void deberia_calcularDisponibilidadPool_cuandoMultiplesRangos() {
        var suero1 = buildSuero(1L, TipoPaciente.PROBLEMA);
        suero1.setRango(1);
        var suero2 = buildSuero(2L, TipoPaciente.PROBLEMA);
        suero2.setRango(2);

        var tubo1 = new Tubo();
        tubo1.setSuero(suero1);
        tubo1.setCantidadInicial(BigDecimal.valueOf(0.6));
        tubo1.setCantidadUsada(BigDecimal.valueOf(0.2));

        var tubo2 = new Tubo();
        tubo2.setSuero(suero2);
        tubo2.setCantidadInicial(BigDecimal.valueOf(0.5));
        tubo2.setCantidadUsada(BigDecimal.valueOf(0.1));

        when(tuboRepository.findBySueroActivoTrue()).thenReturn(List.of(tubo1, tubo2));

        var result = service.getDisponibilidadPool();

        assertThat(result).hasSize(2);
    }

    // --- Util: SueroRangoUtil ---

    @Test
    void deberia_calcularRango0_cuandoBTUMenorOIgual1313() {
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(0.0))).isEqualTo(0);
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(1313.0))).isEqualTo(0);
    }

    @Test
    void deberia_calcularRango1_cuandoBTUEntre1314y2500() {
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(1314.0))).isEqualTo(1);
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(2500.0))).isEqualTo(1);
    }

    @Test
    void deberia_calcularRango2_cuandoBTUEntre2501y8000() {
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(2501.0))).isEqualTo(2);
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(8000.0))).isEqualTo(2);
    }

    @Test
    void deberia_calcularRango3_cuandoBTUMayorA8000() {
        assertThat(SueroRangoUtil.calcularRango(BigDecimal.valueOf(8001.0))).isEqualTo(3);
    }

    // --- Helpers ---

    private Suero buildSuero(Long id, TipoPaciente tipo) {
        var paciente = buildPaciente(id, tipo);
        var suero = new Suero();
        suero.setId(id);
        suero.setActivo(true);
        suero.setPaciente(paciente);
        suero.setFechaExtraccion(LocalDate.now());
        suero.setValorAnticuerpos(BigDecimal.valueOf(1500.0));
        suero.setRango(1);
        suero.setUso(tipo == TipoPaciente.CONTROL ? SueroUso.CONTROL : SueroUso.PROBLEMA);
        return suero;
    }

    private Paciente buildPaciente(Long id, TipoPaciente tipo) {
        var p = new Paciente();
        p.setId(id);
        p.setActivo(true);
        p.setConsentimientoFirmado(true);
        p.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        p.setTipoPaciente(tipo);
        return p;
    }

    private Caja buildCaja(Long id) {
        var c = new Caja();
        c.setId(id);
        c.setActivo(true);
        return c;
    }

    private SueroListDTO buildListDTO(Long id) {
        // Long id, Long pacienteId, String codigoNumerico, BigDecimal valorAnticuerpos,
        // Integer rango, SueroUso uso, BigDecimal cantidadRestante, BigDecimal cantidadTotal, LocalDate fechaExtraccion
        return new SueroListDTO(id, id, "PAC-001", new BigDecimal("1500"), 1, SueroUso.PROBLEMA, BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), LocalDate.now());
    }

    private SueroResponseDTO buildResponseDTO(Long id) {
        // Long id, Long pacienteId, String codigoNumerico, Long cajaId, String freezer,
        // Integer cajon, Integer cajaNumero, List<TuboDTO> tubos, LocalDate fechaExtraccion,
        // BigDecimal cantidadTotal, BigDecimal cantidadRestante, BigDecimal valorAnticuerpos,
        // Integer rango, SueroUso uso, boolean activo, LocalDateTime createdAt
        return new SueroResponseDTO(id, id, "PAC-001", 1L, "A", 1, 1, List.of(),
                LocalDate.now(), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), new BigDecimal("1500"), 1, SueroUso.PROBLEMA, true, null);
    }
}
