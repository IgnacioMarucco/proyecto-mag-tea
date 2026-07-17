package com.utn.magtea.modeloanimal;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.common.DomainConstants;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.pool.PoolSueroAporte;
import com.utn.magtea.storage.Documento;
import com.utn.magtea.storage.DocumentoRepository;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ModeloAnimalServiceTest {

    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(AHORA, ZoneId.of("UTC"));

    @Mock private ModeloAnimalRepository repository;
    @org.mockito.Spy private ModeloAnimalMapper mapper = org.mapstruct.factory.Mappers.getMapper(ModeloAnimalMapper.class);
    @Mock private PoolRepository poolRepository;
    @Mock private TuboRepository tuboRepository;
    @Mock private ModeloAnimalPoolAporteRepository modeloAnimalPoolAporteRepository;
    @Mock private CamadaRepository camadaRepository;
    @Mock private Clock clock;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private ImagenMicroscopiaRepository imagenMicroscopiaRepository;

    @InjectMocks private ModeloAnimalService service;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        lenient().when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
    }

    // --- findAll ---

    @Test
    void deberia_listarModelosAnimales_cuandoExisten() {
        var m = buildBaseModelo();
        var page = new PageImpl<>(List.of(m));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, null, null, null, null, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void deberia_listarModelosAnimales_cuandoFiltroPoolYSexo() {
        var page = new PageImpl<ModeloAnimal>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, 10L, List.of(SexoRaton.MACHO), null, null, null, null, "identificador", "asc");

        assertThat(result.content()).isEmpty();
    }

    @Test
    void deberia_delegarFiltroSoloAlertas_cuandoEsTrue() {
        var page = new PageImpl<ModeloAnimal>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 20, null, null, null, null, null, null, true, "fechaNacimiento", "desc");

        assertThat(result).isNotNull();
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberia_listarModelosAnimales_cuandoFiltroPorTextoLibre() {
        var page = new PageImpl<ModeloAnimal>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, "M-1", null, null, null, null, null, null, "createdAt", "desc");

        assertThat(result.content()).isEmpty();
    }

    @Test
    void deberia_listarModelosAnimales_cuandoFiltroPorUsoYRango() {
        var page = new PageImpl<ModeloAnimal>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, null, null, List.of(SueroUso.CONTROL), List.of(1), null, null, "createdAt", "desc");

        assertThat(result.content()).isEmpty();
    }

    @Test
    void deberia_listarModelosAnimales_cuandoFiltroPorEstado() {
        var page = new PageImpl<ModeloAnimal>(List.of());
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, null, null, null, null, List.of(EstadoProtocolo.COMPLETO), null, "createdAt", "desc");

        assertThat(result.content()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_ejecutarSpecification_paraCobertura() {
        var page = new PageImpl<ModeloAnimal>(List.of());
        var specCaptor = org.mockito.ArgumentCaptor.forClass(Specification.class);
        when(repository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(page);

        // Todos los filtros activos a la vez para ejercitar cada predicate builder de buildSpec
        service.findAll(0, 20, "M-1", 10L, List.of(SexoRaton.MACHO), List.of(SueroUso.CONTROL), List.of(1),
                List.of(EstadoProtocolo.COMPLETO), true, "createdAt", "desc");

        Specification<ModeloAnimal> capturedSpec = specCaptor.getValue();
        assertThat(capturedSpec).isNotNull();

        var root = mock(jakarta.persistence.criteria.Root.class);
        var query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        var cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        var path = mock(jakarta.persistence.criteria.Path.class);
        var expr = mock(jakarta.persistence.criteria.Expression.class);
        var predicate = mock(jakarta.persistence.criteria.Predicate.class);

        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(path.get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn(expr);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.isTrue(any())).thenReturn(predicate);
        lenient().when(cb.lessThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);
        lenient().when(path.in(any(java.util.Collection.class))).thenReturn(predicate);
        lenient().when(cb.and(any(jakarta.persistence.criteria.Predicate.class), any(jakarta.persistence.criteria.Predicate.class)))
                .thenReturn(predicate);
        lenient().when(cb.or(any(jakarta.persistence.criteria.Predicate.class), any(jakarta.persistence.criteria.Predicate.class)))
                .thenReturn(predicate);
        lenient().when(cb.or(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(predicate);

        capturedSpec.toPredicate(root, query, cb);
    }

    // --- findById ---

    @Test
    void deberia_calcularNecesitaVocalizaciones_cuandoDia5YSinVocalizaciones() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(hoy.minusDays(5));
        m.setVocalizaciones(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isTrue();
    }

    @Test
    void deberia_noNecesitarVocalizaciones_cuandoYaRegistradas() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(hoy.minusDays(5));
        m.setVocalizaciones(new VocalizacionesUltrasonicas());

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isFalse();
    }

    @Test
    void deberia_noNecesitarVocalizaciones_cuandoDiaNoCorrecto() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(hoy.minusDays(3));
        m.setVocalizaciones(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isFalse();
    }

    @Test
    void deberia_calcularNecesitaTresCamaras_cuandoDia19YSinEstudio() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(hoy.minusDays(19));
        m.setTresCamaras(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaTresCamaras()).isTrue();
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoModeloNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modelo animal con id 99 no existe");
    }

    // --- create ---

    @Test
    void deberia_crearModeloAnimal_cuandoDatosValidos() {
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.HEMBRA, List.of());

        var pool = buildPool(10L);
        var camada = buildCamada(20L);
        var m = buildBaseModelo();
        m.setId(2L);

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(0L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(m);
        when(repository.findById(2L)).thenReturn(Optional.of(m));

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        verify(repository, times(2)).save(any(ModeloAnimal.class));
    }

    @Test
    void deberia_generarIdentificadorAutomatico_cuandoEsPrimerRatonDelPool() {
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.HEMBRA, null);

        var pool = buildPool(10L);
        pool.setCodigo("ABC123");
        var camada = buildCamada(20L);
        var saved = buildBaseModelo();
        saved.setId(5L);

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(0L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(saved);
        when(repository.findById(5L)).thenReturn(Optional.of(saved));

        service.create(dto);

        verify(repository).save(argThat(ma -> "ABC123-1".equals(ma.getIdentificador())));
    }

    @Test
    void deberia_generarIdentificadorConSufijoCorrecto_cuandoYaHayRatonesEnElPool() {
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null);

        var pool = buildPool(10L);
        pool.setCodigo("ABC123");
        var camada = buildCamada(20L);
        var saved = buildBaseModelo();
        saved.setId(6L);

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(2L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(saved);
        when(repository.findById(6L)).thenReturn(Optional.of(saved));

        service.create(dto);

        verify(repository).save(argThat(ma -> "ABC123-3".equals(ma.getIdentificador())));
    }

    @Test
    void deberia_generarIdentificadorNumerico_cuandoHayMasDe26Ratones() {
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.HEMBRA, null);

        var pool = buildPool(10L);
        pool.setCodigo("ABC123");
        var camada = buildCamada(20L);
        var saved = buildBaseModelo();
        saved.setId(27L);

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(26L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(saved);
        when(repository.findById(27L)).thenReturn(Optional.of(saved));

        service.create(dto);

        verify(repository).save(argThat(ma -> "ABC123-27".equals(ma.getIdentificador())));
    }

    @Test
    void deberia_crearModeloAnimal_cuandoTieneAportes() {
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, List.of(aporte));

        var pool = buildPool(10L);
        var camada = buildCamada(20L);
        var m = buildBaseModelo();
        m.setId(3L);

        var tuboPool = new Tubo();
        tuboPool.setId(100L);
        tuboPool.setTipo(TipoTubo.POOL);
        tuboPool.setPool(pool);
        tuboPool.setCantidadInicial(BigDecimal.valueOf(1.0));
        tuboPool.setCantidadUsada(BigDecimal.ZERO);

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(0L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(m);
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboPool));
        when(repository.findById(3L)).thenReturn(Optional.of(m));

        var result = service.create(dto);

        assertThat(result).isNotNull();
        verify(modeloAnimalPoolAporteRepository).save(any(ModeloAnimalPoolAporte.class));
        verify(tuboRepository).save(tuboPool);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPoolNoExisteAlCrear() {
        var dto = new ModeloAnimalCreateDTO(99L, 20L, SexoRaton.MACHO, null);

        when(poolRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pool con id 99 no existe");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaNoExisteAlCrear() {
        var dto = new ModeloAnimalCreateDTO(10L, 99L, SexoRaton.MACHO, null);

        var pool = buildPool(10L);
        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 99 no existe");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoTuboAporteNoEsDePool() {
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, List.of(aporte));

        var pool = buildPool(10L);
        var camada = buildCamada(20L);
        var m = buildBaseModelo();

        var tuboSuero = new Tubo();
        tuboSuero.setId(100L);
        tuboSuero.setTipo(TipoTubo.SUERO);
        tuboSuero.setPosicion("A1");

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(0L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(m);
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboSuero));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no es un tubo de pool");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTuboAporteNoExisteAlCrear() {
        var aporte = new ModeloAnimalPoolAporteInputDTO(999L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, List.of(aporte));

        var pool = buildPool(10L);
        var camada = buildCamada(20L);
        var m = buildBaseModelo();

        when(poolRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.countByPool_Id(10L)).thenReturn(0L);
        when(repository.save(any(ModeloAnimal.class))).thenReturn(m);
        when(tuboRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tubo con id 999 no existe");
    }

    // --- update ---

    @Test
    void deberia_actualizarModeloAnimal_cuandoDatosValidos() {
        var m = buildBaseModelo();
        var pool = buildPool(10L);
        var camada = buildCamada(20L);
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.HEMBRA, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(poolRepository.findById(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camada));
        when(repository.save(m)).thenReturn(m);

        var result = service.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(m.getSexo()).isEqualTo(SexoRaton.HEMBRA);
        verify(repository).save(m);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoModeloNoExisteAlActualizar() {
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modelo animal con id 99 no existe");
    }

    // --- registrarVocalizaciones ---

    @Test
    void deberia_registrarVocalizaciones_cuandoModeloExiste() {
        var m = buildBaseModelo();
        m.setVocalizaciones(null);
        var dto = new VocalizacionesDTO(30.0, 60.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarVocalizaciones(1L, dto);

        assertThat(result.vocalizaciones()).isNotNull();
        assertThat(m.getVocalizaciones().getMuestra1Khz()).isEqualTo(30.0);
        verify(repository).save(m);
    }

    @Test
    void deberia_actualizarVocalizaciones_cuandoYaExistian() {
        var m = buildBaseModelo();
        var vusExistente = new VocalizacionesUltrasonicas();
        vusExistente.setId(5L);
        vusExistente.setMuestra1Khz(10.0);
        vusExistente.setMuestra2Khz(20.0);
        m.setVocalizaciones(vusExistente);

        var dto = new VocalizacionesDTO(40.0, 80.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarVocalizaciones(1L, dto);

        assertThat(m.getVocalizaciones().getId()).isEqualTo(5L);
        assertThat(m.getVocalizaciones().getMuestra1Khz()).isEqualTo(40.0);
    }

    @Test
    void deberia_registrarVocalizaciones_cuandoEsAntesDelDia7() {
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(LocalDate.now(FIXED_CLOCK).minusDays(5));

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarVocalizaciones(1L, new VocalizacionesDTO(30.0, 60.0, null, null));

        assertThat(result.vocalizaciones()).isNotNull();
        assertThat(m.getVocalizaciones().getMuestra1Khz()).isEqualTo(30.0);
    }

    @Test
    void deberia_registrarVocalizaciones_cuandoEstadoEsPendienteInoculacion() {
        var m = buildBaseModelo();
        // estadoProtocolo default = PENDIENTE_INOCULACION, sin forzar nada

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarVocalizaciones(1L, new VocalizacionesDTO(30.0, 60.0, null, null));

        assertThat(result.vocalizaciones()).isNotNull();
    }

    // --- registrarTresCamaras ---

    @Test
    void deberia_registrarTresCamaras_cuandoModeloExiste() {
        var m = buildBaseModelo();
        m.setTresCamaras(null);
        var dto = new TresCamarasDTO(10.0, 5.0, 8.0, 4.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarTresCamaras(1L, dto);

        assertThat(result.tresCamaras()).isNotNull();
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(10.0);
        verify(repository).save(m);
    }

    @Test
    void deberia_registrarTresCamaras_cuandoEsAntesDelDia21() {
        var m = buildBaseModelo();
        m.getCamada().setFechaNacimiento(LocalDate.now(FIXED_CLOCK).minusDays(15));

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarTresCamaras(1L, new TresCamarasDTO(10.0, 5.0, 8.0, 4.0, null, null));

        assertThat(result.tresCamaras()).isNotNull();
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(10.0);
    }

    @Test
    void deberia_registrarTresCamaras_cuandoEstadoEsPendienteInoculacion() {
        var m = buildBaseModelo();
        // estadoProtocolo default = PENDIENTE_INOCULACION, sin forzar nada

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarTresCamaras(1L, new TresCamarasDTO(10.0, 5.0, 8.0, 4.0, null, null));

        assertThat(result.tresCamaras()).isNotNull();
    }

    @Test
    void deberia_registrarTresCamaras_secuencialmente_sinForzarEstado() {
        var m = buildBaseModelo();
        // sin m.setEstadoProtocolo(...) manual: reproduce la secuencia real de uso

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarTresCamaras(1L, new TresCamarasDTO(10.0, 5.0, null, null, null, null)); // solo M1
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(10.0);
        assertThat(m.getTresCamaras().getM2TiempoRatonDesconocido()).isNull();

        // El estado ya avanzó más allá de PENDIENTE_TRES_CAMARAS (m.getTresCamaras() != null);
        // antes de esta corrección, esta segunda llamada hubiera sido rechazada.
        service.registrarTresCamaras(1L, new TresCamarasDTO(null, null, 8.0, 4.0, null, null)); // solo M2
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(10.0); // preservado
        assertThat(m.getTresCamaras().getM2TiempoRatonDesconocido()).isEqualTo(8.0);
    }

    @Test
    void deberia_permitirEditarVocalizaciones_cuandoEstadoYaEsCompleto() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(21));
        for (int dia = 1; dia <= 4; dia++) {
            var aporte = new ModeloAnimalPoolAporte();
            aporte.setDia(dia);
            m.getAportes().add(aporte);
        }
        var vus = new VocalizacionesUltrasonicas();
        vus.setMuestra1Khz(30.0);
        vus.setMuestra2Khz(60.0);
        m.setVocalizaciones(vus);
        var tc = new TresCamaras();
        tc.setM1TiempoRatonNovedad(10.0);
        tc.setM1TiempoObjetoNovedoso(5.0);
        tc.setM2TiempoRatonDesconocido(8.0);
        tc.setM2TiempoRatonFamiliar(4.0);
        m.setTresCamaras(tc);
        m.setNumCelulasGanglionares(100);
        m.setNumCelulasPurkinje(50);
        m.setEstadoProtocolo(EstadoProtocolo.COMPLETO);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarVocalizaciones(1L, new VocalizacionesDTO(99.0, 60.0, null, null));

        assertThat(m.getVocalizaciones().getMuestra1Khz()).isEqualTo(99.0);
        assertThat(result.estadoProtocolo()).isEqualTo(EstadoProtocolo.COMPLETO);
    }

    // --- registrarMicroscopia ---

    @Test
    void deberia_registrarMicroscopia_cuandoModeloExiste() {
        var m = buildBaseModelo();
        var dto = new ModeloAnimalMicroscopiaDTO(150, 80);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarMicroscopia(1L, dto);

        assertThat(result).isNotNull();
        assertThat(m.getNumCelulasGanglionares()).isEqualTo(150);
        assertThat(m.getNumCelulasPurkinje()).isEqualTo(80);
        verify(repository).save(m);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoModeloNoExisteAlRegistrarMicroscopia() {
        var dto = new ModeloAnimalMicroscopiaDTO(100, 50);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarMicroscopia(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modelo animal con id 99 no existe");
    }

    // --- delete ---

    @Test
    void deberia_eliminarModeloAnimal_cuandoExiste() {
        var m = buildBaseModelo();

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.delete(1L);

        assertThat(m.isActivo()).isFalse();
        verify(repository).save(argThat(ma -> !ma.isActivo()));
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoModeloNoExisteAlEliminar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modelo animal con id 99 no existe");
    }

    // --- Mapper tests (VUS y TresCamaras) ---

    @Test
    void deberia_calcularVusBandaAversiva_cuandoFrecuencia20a50kHz() {
        var vus = new VocalizacionesUltrasonicas();
        vus.setMuestra1Khz(25.0);
        vus.setMuestra2Khz(60.0);

        var dto = mapper.toVocalizacionesDTO(vus);

        assertThat(dto.vusBanda1()).isEqualTo(VusBanda.AVERSIVA);
        assertThat(dto.vusBanda2()).isEqualTo(VusBanda.APETITIVA);
    }

    @Test
    void deberia_calcularVusBandaApetitiva_cuandoFrecuenciaSobre55kHz() {
        var vus = new VocalizacionesUltrasonicas();
        vus.setMuestra1Khz(70.0);
        vus.setMuestra2Khz(30.0);

        var dto = mapper.toVocalizacionesDTO(vus);

        assertThat(dto.vusBanda1()).isEqualTo(VusBanda.APETITIVA);
        assertThat(dto.vusBanda2()).isEqualTo(VusBanda.AVERSIVA);
    }

    @Test
    void deberia_calcularFaltaSocializacion_cuandoRatioM1MenorOIgualA1() {
        var tc = new TresCamaras();
        tc.setM1TiempoRatonNovedad(10.0);
        tc.setM1TiempoObjetoNovedoso(10.0);
        tc.setM2TiempoRatonDesconocido(5.0);
        tc.setM2TiempoRatonFamiliar(10.0);

        var dto = mapper.toTresCamarasDTO(tc);

        assertThat(dto.sociabilizacion1()).isEqualTo(SocializacionResultado.FALTA_SOCIALIZACION);
        assertThat(dto.sociabilizacion2()).isEqualTo(SocializacionResultado.FALTA_SOCIALIZACION);
    }

    @Test
    void deberia_calcularNormal_cuandoRatioM1MayorA1() {
        var tc = new TresCamaras();
        tc.setM1TiempoRatonNovedad(15.0);
        tc.setM1TiempoObjetoNovedoso(10.0);
        tc.setM2TiempoRatonDesconocido(12.0);
        tc.setM2TiempoRatonFamiliar(10.0);

        var dto = mapper.toTresCamarasDTO(tc);

        assertThat(dto.sociabilizacion1()).isEqualTo(SocializacionResultado.NORMAL);
        assertThat(dto.sociabilizacion2()).isEqualTo(SocializacionResultado.NORMAL);
    }

    // --- update: cambio de pool ---

    @Test
    void deberia_lanzarBusinessRuleException_cuandoCambiarPoolEnUpdate() {
        var m = buildBaseModelo(); // pool.id = 10L
        var dto = new ModeloAnimalCreateDTO(99L, 20L, SexoRaton.MACHO, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede cambiar el pool");
    }

    // --- findByIdentificador ---

    @Test
    void deberia_obtenerModeloPorIdentificador_cuandoExiste() {
        var m = buildBaseModelo();

        when(repository.findByIdentificadorAndActivoTrue("M-1")).thenReturn(Optional.of(m));

        var result = service.findByIdentificador("M-1");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoIdentificadorNoExiste() {
        when(repository.findByIdentificadorAndActivoTrue("INVALIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIdentificador("INVALIDO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    // --- registrarInoculacion ---

    @Test
    void deberia_registrarInoculacion_cuandoSinAportes() {
        var m = buildBaseModelo();
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarInoculacion(1L, dto);

        assertThat(result).isNotNull();
        assertThat(m.getFechaDia1Inoculacion()).isEqualTo(LocalDate.now(FIXED_CLOCK));
        verify(repository).save(m);
    }

    @Test
    void deberia_registrarInoculacion_cuandoConAportes() {
        var m = buildBaseModelo(); // pool.id = 10L
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboPool = buildTuboPool(100L, 10L, BigDecimal.valueOf(1.0), BigDecimal.ZERO);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.empty());
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboPool));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarInoculacion(1L, dto);

        assertThat(result).isNotNull();
        verify(modeloAnimalPoolAporteRepository).save(any(ModeloAnimalPoolAporte.class));
        verify(tuboRepository).save(tuboPool);
    }

    @Test
    void deberia_registrarInoculacion_cuandoUpsertAporteExistente() {
        var m = buildBaseModelo(); // pool.id = 10L
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboPool = buildTuboPool(100L, 10L, BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.05));

        // Aporte previo existente en día 1 (consumió 0.05 del mismo tubo)
        var prevAporte = new ModeloAnimalPoolAporte();
        prevAporte.setId(99L);
        prevAporte.setTubo(tuboPool);
        prevAporte.setCantidadConsumida(BigDecimal.valueOf(0.05));
        prevAporte.setDia(1);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.of(prevAporte));
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboPool));
        when(repository.save(m)).thenReturn(m);

        service.registrarInoculacion(1L, dto);

        // Reutiliza la MISMA fila (no delete + insert) para no violar uc_aporte_animal_dia.
        verify(modeloAnimalPoolAporteRepository, never()).delete(any());
        verify(modeloAnimalPoolAporteRepository).save(prevAporte);
        assertThat(prevAporte.getCantidadConsumida()).isEqualByComparingTo(BigDecimal.valueOf(0.1));
        // Revierte el consumo viejo (0.05) y aplica el nuevo (0.1) → usada neta 0.1.
        assertThat(tuboPool.getCantidadUsada()).isEqualByComparingTo(BigDecimal.valueOf(0.1));
    }

    @Test
    void deberia_editarInoculacion_revirtiendoTuboAnterior_cuandoCambiaDeTubo() {
        var m = buildBaseModelo(); // pool.id = 10L
        // Se edita el día 1: antes se usó el tubo 100 (0.05), ahora se usa el tubo 101 (0.1)
        var aporte = new ModeloAnimalPoolAporteInputDTO(101L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboAnterior = buildTuboPool(100L, 10L, BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.05));
        var tuboNuevo    = buildTuboPool(101L, 10L, BigDecimal.valueOf(1.0), BigDecimal.ZERO);

        var prevAporte = new ModeloAnimalPoolAporte();
        prevAporte.setId(99L);
        prevAporte.setTubo(tuboAnterior);
        prevAporte.setCantidadConsumida(BigDecimal.valueOf(0.05));
        prevAporte.setDia(1);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.of(prevAporte));
        when(tuboRepository.findById(101L)).thenReturn(Optional.of(tuboNuevo));
        when(repository.save(m)).thenReturn(m);

        service.registrarInoculacion(1L, dto);

        verify(modeloAnimalPoolAporteRepository, never()).delete(any());
        // El tubo anterior recupera su volumen (0.05 → 0.0) y el nuevo lo descuenta (0.0 → 0.1).
        assertThat(tuboAnterior.getCantidadUsada()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(tuboNuevo.getCantidadUsada()).isEqualByComparingTo(BigDecimal.valueOf(0.1));
        assertThat(prevAporte.getTubo()).isEqualTo(tuboNuevo);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoTuboInoculacionNoEsDePool() {
        var m = buildBaseModelo();
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboSuero = new Tubo();
        tuboSuero.setId(100L);
        tuboSuero.setTipo(TipoTubo.SUERO);
        tuboSuero.setPosicion("A1");

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.empty());
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboSuero));

        assertThatThrownBy(() -> service.registrarInoculacion(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no es un tubo de pool");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTuboInoculacionNoExiste() {
        var m = buildBaseModelo();
        var aporte = new ModeloAnimalPoolAporteInputDTO(999L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.empty());
        when(tuboRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarInoculacion(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tubo con id 999 no existe");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoTuboNoPerteneceMismoPoolEnInoculacion() {
        var m = buildBaseModelo(); // pool.id = 10L
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.1), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboOtroPool = buildTuboPool(100L, 99L, BigDecimal.valueOf(1.0), BigDecimal.ZERO); // pool.id = 99L (distinto)

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.empty());
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboOtroPool));

        assertThatThrownBy(() -> service.registrarInoculacion(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no pertenece al pool");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoVolumenInsuficienteEnInoculacion() {
        var m = buildBaseModelo(); // pool.id = 10L
        var aporte = new ModeloAnimalPoolAporteInputDTO(100L, BigDecimal.valueOf(0.5), 1);
        var dto = new ModeloAnimalInoculacionDTO(LocalDate.now(FIXED_CLOCK), List.of(aporte));

        var tuboPool = buildTuboPool(100L, 10L, BigDecimal.valueOf(0.3), BigDecimal.ZERO); // disponible 0.3 < 0.5

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(1L, 1)).thenReturn(Optional.empty());
        when(tuboRepository.findById(100L)).thenReturn(Optional.of(tuboPool));

        assertThatThrownBy(() -> service.registrarInoculacion(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no tiene suficiente volumen");
    }

    // --- agregarImagen ---

    @Test
    void deberia_agregarImagen_cuandoTieneUrlExterna() {
        var m = buildBaseModelo();
        var dto = new ImagenMicroscopiaCreateDTO(TipoImagenMicroscopia.GANGLIONAR, null, "https://example.com/img.jpg", "descripcion");

        var imagenSaved = new ImagenMicroscopia();
        imagenSaved.setId(10L);
        imagenSaved.setTipo(TipoImagenMicroscopia.GANGLIONAR);
        imagenSaved.setUrlExterna("https://example.com/img.jpg");
        imagenSaved.setDescripcion("descripcion");

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(imagenMicroscopiaRepository.save(any(ImagenMicroscopia.class))).thenReturn(imagenSaved);

        var result = service.agregarImagen(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.urlExterna()).isEqualTo("https://example.com/img.jpg");
        verify(imagenMicroscopiaRepository).save(any(ImagenMicroscopia.class));
    }

    @Test
    void deberia_agregarImagen_cuandoTieneDocumento() {
        var m = buildBaseModelo();
        var dto = new ImagenMicroscopiaCreateDTO(TipoImagenMicroscopia.PURKINJE, 5L, null, null);

        var doc = new Documento();
        doc.setId(5L);

        var imagenSaved = new ImagenMicroscopia();
        imagenSaved.setId(11L);
        imagenSaved.setTipo(TipoImagenMicroscopia.PURKINJE);
        imagenSaved.setDocumento(doc);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(documentoRepository.findById(5L)).thenReturn(Optional.of(doc));
        when(imagenMicroscopiaRepository.save(any(ImagenMicroscopia.class))).thenReturn(imagenSaved);

        var result = service.agregarImagen(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.documentoId()).isEqualTo(5L);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoAgregarImagenSinDocumentoNiUrl() {
        var m = buildBaseModelo();
        var dto = new ImagenMicroscopiaCreateDTO(TipoImagenMicroscopia.GANGLIONAR, null, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.agregarImagen(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("documento subido o una URL externa");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoDocumentoNoExisteAlAgregarImagen() {
        var m = buildBaseModelo();
        var dto = new ImagenMicroscopiaCreateDTO(TipoImagenMicroscopia.GANGLIONAR, 99L, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agregarImagen(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Documento con id 99 no existe");
    }

    // --- eliminarImagen ---

    @Test
    void deberia_eliminarImagen_cuandoExiste() {
        var m = buildBaseModelo();

        var imagen = new ImagenMicroscopia();
        imagen.setId(10L);
        imagen.setModeloAnimal(m);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(imagenMicroscopiaRepository.findById(10L)).thenReturn(Optional.of(imagen));

        service.eliminarImagen(1L, 10L);

        verify(imagenMicroscopiaRepository).delete(imagen);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoImagenNoExisteAlEliminar() {
        var m = buildBaseModelo();

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(imagenMicroscopiaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarImagen(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Imagen con id 99 no existe");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoImagenNoPertenecePorModelo() {
        var m = buildBaseModelo(); // id = 1L

        var otroModelo = new ModeloAnimal();
        otroModelo.setId(999L);
        otroModelo.setActivo(true);

        var imagen = new ImagenMicroscopia();
        imagen.setId(10L);
        imagen.setModeloAnimal(otroModelo); // pertenece a otro modelo

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(imagenMicroscopiaRepository.findById(10L)).thenReturn(Optional.of(imagen));

        assertThatThrownBy(() -> service.eliminarImagen(1L, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no pertenece al modelo animal");
    }

    // --- getReporte ---

    @Test
    void deberia_obtenerReporte_cuandoModeloConAportes() {
        var caja = buildCaja();
        var pool = buildPool(10L);
        pool.setCaja(caja);
        pool.setFechaCreacion(LocalDate.now(FIXED_CLOCK).minusDays(30));
        pool.setUso(SueroUso.CONTROL);

        var paciente = new com.utn.magtea.paciente.Paciente();
        paciente.setCodigoNumerico("TST00001");
        paciente.setTipoPaciente(com.utn.magtea.paciente.TipoPaciente.PROBLEMA);

        var suero = new Suero();
        suero.setId(50L);
        suero.setValorAnticuerpos(new BigDecimal("150.0"));
        suero.setRango(2);
        suero.setFechaExtraccion(LocalDate.now(FIXED_CLOCK).minusDays(10));
        suero.setPaciente(paciente);

        var tuboSuero = new Tubo();
        tuboSuero.setId(200L);
        tuboSuero.setTipo(TipoTubo.SUERO);
        tuboSuero.setSuero(suero);

        var poolAporte = new PoolSueroAporte();
        poolAporte.setId(1L);
        poolAporte.setTubo(tuboSuero);
        pool.getAportes().add(poolAporte);

        // Segundo aporte del pool sobre el MISMO suero (otro tubo): ejercita la deduplicación por Suero::getId
        var tuboSueroBis = new Tubo();
        tuboSueroBis.setId(201L);
        tuboSueroBis.setTipo(TipoTubo.SUERO);
        tuboSueroBis.setSuero(suero);
        var poolAporteBis = new PoolSueroAporte();
        poolAporteBis.setId(2L);
        poolAporteBis.setTubo(tuboSueroBis);
        pool.getAportes().add(poolAporteBis);

        var aporteMA = new ModeloAnimalPoolAporte();
        aporteMA.setDia(1);
        aporteMA.setCantidadConsumida(new BigDecimal("0.1"));
        aporteMA.setTubo(tuboSuero);

        var modelo = buildBaseModelo();
        modelo.setPool(pool);
        modelo.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(4));
        modelo.getAportes().add(aporteMA);

        when(repository.findByIdentificadorForReporte("M-1")).thenReturn(Optional.of(modelo));

        var result = service.getReporte("M-1");

        assertThat(result).isNotNull();
        assertThat(result.identificador()).isEqualTo("M-1");
        assertThat(result.inoculaciones()).hasSize(1);
        assertThat(result.inoculaciones().get(0).dia()).isEqualTo(1);
        assertThat(result.pool().codigo()).isEqualTo("XY1234");
        // hasSize(1): aunque hay 2 aportes del pool, ambos apuntan al mismo Suero (id=50) → se deduplica
        assertThat(result.sueros()).hasSize(1);
        assertThat(result.sueros().get(0).valorAnticuerpos()).isEqualByComparingTo(new BigDecimal("150.0"));
        assertThat(result.sueros().get(0).paciente()).isNotNull();
        assertThat(result.sueros().get(0).paciente().codigoNumerico()).isEqualTo("TST00001");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoIdentificadorNoExisteEnReporte() {
        when(repository.findByIdentificadorForReporte("INVALIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReporte("INVALIDO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    // --- registrarTresCamaras — caminos adicionales ---

    @Test
    void deberia_actualizarTresCamaras_cuandoYaExistian() {
        var m = buildBaseModelo();
        var tcExistente = new TresCamaras();
        tcExistente.setId(7L);
        tcExistente.setM1TiempoRatonNovedad(5.0);
        m.setTresCamaras(tcExistente);
        var dto = new TresCamarasDTO(20.0, 10.0, null, null, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarTresCamaras(1L, dto);

        assertThat(m.getTresCamaras().getId()).isEqualTo(7L);
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(20.0);
        assertThat(m.getTresCamaras().getM2TiempoRatonDesconocido()).isNull();
    }

    @Test
    void deberia_registrarTresCamaras_cuandoSoloM2NonNull() {
        var m = buildBaseModelo();
        var dto = new TresCamarasDTO(null, null, 8.0, 4.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarTresCamaras(1L, dto);

        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isNull();
        assertThat(m.getTresCamaras().getM2TiempoRatonDesconocido()).isEqualTo(8.0);
    }

    // --- update — camada inactiva ---

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaInactivaAlActualizar() {
        var m = buildBaseModelo();
        var pool = buildPool(10L);
        var camadaInactiva = buildCamada(20L);
        camadaInactiva.setActivo(false);
        var dto = new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(poolRepository.findById(10L)).thenReturn(Optional.of(pool));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(camadaInactiva));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 20 no existe");
    }

    // --- calcularEstado y calcularFechaProximoEvento ---

    @Test
    void deberia_calcularEstadoCompleto_cuandoTodoRegistrado() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(5));
        for (int i = 0; i < 4; i++) m.getAportes().add(new ModeloAnimalPoolAporte());
        m.setVocalizaciones(new VocalizacionesUltrasonicas());
        m.setTresCamaras(new TresCamaras());
        // numCelulasGanglionares = null hasta ahora → PENDIENTE_MICROSCOPIA
        var dto = new ModeloAnimalMicroscopiaDTO(200, 100);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarMicroscopia(1L, dto);

        assertThat(m.getEstadoProtocolo()).isEqualTo(EstadoProtocolo.COMPLETO);
        assertThat(m.getFechaProximoEvento()).isNull();
    }

    @Test
    void deberia_calcularFechaProximoEvento_cuandoPendienteVocalizaciones() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(5));
        for (int i = 0; i < 4; i++) m.getAportes().add(new ModeloAnimalPoolAporte());
        // vocalizaciones = null → tras registrar microscopia (sin voc) queda PENDIENTE_VOCALIZACIONES
        var dto = new ModeloAnimalMicroscopiaDTO(150, 80);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        service.registrarMicroscopia(1L, dto);

        var fn = m.getCamada().getFechaNacimiento();
        assertThat(m.getEstadoProtocolo()).isEqualTo(EstadoProtocolo.PENDIENTE_VOCALIZACIONES);
        assertThat(m.getFechaProximoEvento()).isEqualTo(fn.plusDays(DomainConstants.DIA_VOCALIZACIONES));
    }

    @Test
    void deberia_calcularEstadoInoculacionEnCurso_cuandoMenosDeCuatroAportes() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(2));
        for (int i = 0; i < 2; i++) m.getAportes().add(new ModeloAnimalPoolAporte());

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(poolRepository.findById(10L)).thenReturn(Optional.of(m.getPool()));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(m.getCamada()));
        when(repository.save(m)).thenReturn(m);

        // update() no toca aportes/vocalizaciones: solo recalcula estado a partir del estado actual de m
        service.update(1L, new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null));

        assertThat(m.getEstadoProtocolo()).isEqualTo(EstadoProtocolo.INOCULACION_EN_CURSO);
        assertThat(m.getFechaProximoEvento()).isEqualTo(m.getFechaDia1Inoculacion().plusDays(2));
    }

    @Test
    void deberia_calcularEstadoPendienteTresCamaras_cuandoVocalizacionesListasYSinEstudio() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(21));
        for (int i = 0; i < 4; i++) m.getAportes().add(new ModeloAnimalPoolAporte());
        m.setVocalizaciones(new VocalizacionesUltrasonicas());
        m.setTresCamaras(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(poolRepository.findById(10L)).thenReturn(Optional.of(m.getPool()));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(m.getCamada()));
        when(repository.save(m)).thenReturn(m);

        service.update(1L, new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null));

        var fn = m.getCamada().getFechaNacimiento();
        assertThat(m.getEstadoProtocolo()).isEqualTo(EstadoProtocolo.PENDIENTE_TRES_CAMARAS);
        assertThat(m.getFechaProximoEvento()).isEqualTo(fn.plusDays(DomainConstants.DIA_TRES_CAMARAS));
    }

    @Test
    void deberia_calcularEstadoPendienteMicroscopia_cuandoTresCamarasListasYSinCelulas() {
        var m = buildBaseModelo();
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK).minusDays(21));
        for (int i = 0; i < 4; i++) m.getAportes().add(new ModeloAnimalPoolAporte());
        m.setVocalizaciones(new VocalizacionesUltrasonicas());
        m.setTresCamaras(new TresCamaras());
        m.setNumCelulasGanglionares(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(poolRepository.findById(10L)).thenReturn(Optional.of(m.getPool()));
        when(camadaRepository.findById(20L)).thenReturn(Optional.of(m.getCamada()));
        when(repository.save(m)).thenReturn(m);

        service.update(1L, new ModeloAnimalCreateDTO(10L, 20L, SexoRaton.MACHO, null));

        var fn = m.getCamada().getFechaNacimiento();
        assertThat(m.getEstadoProtocolo()).isEqualTo(EstadoProtocolo.PENDIENTE_MICROSCOPIA);
        assertThat(m.getFechaProximoEvento()).isEqualTo(fn.plusDays(DomainConstants.DIA_TRES_CAMARAS));
    }

    @Test
    void deberia_noNecesitarVocalizaciones_cuandoCamadaEsNull() {
        var m = buildBaseModelo();
        m.setCamada(null);
        m.setVocalizaciones(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isFalse();
    }

    @Test
    void deberia_noNecesitarTresCamaras_cuandoCamadaEsNull() {
        var m = buildBaseModelo();
        m.setCamada(null);
        m.setTresCamaras(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaTresCamaras()).isFalse();
    }

    // --- Helpers ---

    private ModeloAnimal buildBaseModelo() {
        var m = new ModeloAnimal();
        m.setId(1L);
        m.setIdentificador("M-1");
        m.setActivo(true);
        m.setSexo(SexoRaton.MACHO);

        var pool = buildPool(10L);
        m.setPool(pool);

        var camada = buildCamada(20L);
        m.setCamada(camada);

        return m;
    }

    private Pool buildPool(Long id) {
        var pool = new Pool();
        pool.setId(id);
        pool.setActivo(true);
        pool.setRango(1);
        pool.setCodigo("XY1234");
        return pool;
    }

    private Tubo buildTuboPool(Long tuboId, Long poolId, BigDecimal cantidadInicial, BigDecimal cantidadUsada) {
        var pool = new Pool();
        pool.setId(poolId);
        pool.setActivo(true);

        var t = new Tubo();
        t.setId(tuboId);
        t.setTipo(TipoTubo.POOL);
        t.setPool(pool);
        t.setPosicion("P1");
        t.setCantidadInicial(cantidadInicial);
        t.setCantidadUsada(cantidadUsada);
        return t;
    }

    private Camada buildCamada(Long id) {
        var camada = new Camada();
        camada.setId(id);
        camada.setNombre("C-1");
        camada.setActivo(true);
        camada.setFechaNacimiento(LocalDate.now(FIXED_CLOCK).minusDays(21));
        return camada;
    }

    private Caja buildCaja() {
        var c = new Caja();
        c.setId(1L);
        c.setFreezer("F1");
        c.setCajon(1);
        c.setNumero(5);
        return c;
    }
}
