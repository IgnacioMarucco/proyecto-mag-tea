package com.utn.magtea.modeloanimal;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.storage.DocumentoRepository;
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

        var result = service.findAll(0, 10, null, null, null, null, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void deberia_listarModelosAnimales_cuandoFiltroPoolYSexo() {
        var page = new PageImpl<ModeloAnimal>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, 10L, SexoRaton.MACHO, null, null, null, "identificador", "asc");

        assertThat(result.content()).isEmpty();
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
        verify(repository).save(any(ModeloAnimal.class));
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

    private Camada buildCamada(Long id) {
        var camada = new Camada();
        camada.setId(id);
        camada.setNombre("C-1");
        camada.setActivo(true);
        camada.setFechaNacimiento(LocalDate.now(FIXED_CLOCK));
        return camada;
    }
}
