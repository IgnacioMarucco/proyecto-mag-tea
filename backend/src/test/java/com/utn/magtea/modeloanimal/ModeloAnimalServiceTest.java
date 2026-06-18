package com.utn.magtea.modeloanimal;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ModeloAnimalServiceTest {

    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(AHORA, ZoneId.of("UTC"));

    @Mock private ModeloAnimalRepository repository;
    @org.mockito.Spy private ModeloAnimalMapper mapper = org.mapstruct.factory.Mappers.getMapper(ModeloAnimalMapper.class);
    @Mock private PoolRepository poolRepository;
    @Mock private CamadaRepository camadaRepository;
    @Mock private Clock clock;

    @InjectMocks private ModeloAnimalService service;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        lenient().when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
    }

    @Test
    void deberia_calcularNecesitaVocalizaciones_cuandoDia5YSinVocalizaciones() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.setFechaNacimiento(hoy.minusDays(5));
        m.setVocalizaciones(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isTrue();
    }

    @Test
    void deberia_noNecesitarVocalizaciones_cuandoYaRegistradas() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.setFechaNacimiento(hoy.minusDays(5));
        m.setVocalizaciones(new VocalizacionesUltrasonicas());

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isFalse();
    }

    @Test
    void deberia_noNecesitarVocalizaciones_cuandoDiaNoCorrecto() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.setFechaNacimiento(hoy.minusDays(3)); // día 3, no 5
        m.setVocalizaciones(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaVocalizaciones()).isFalse();
    }

    @Test
    void deberia_calcularNecesitaTresCamaras_cuandoDia19YSinEstudio() {
        var hoy = LocalDate.now(FIXED_CLOCK);
        var m = buildBaseModelo();
        m.setFechaNacimiento(hoy.minusDays(19));
        m.setTresCamaras(null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));

        var result = service.findById(1L);

        assertThat(result.necesitaTresCamaras()).isTrue();
    }

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

    @Test
    void deberia_registrarVocalizaciones_cuandoModeloExiste() {
        var m = buildBaseModelo();
        m.setVocalizaciones(null);
        var dto = new VocalizacionesDTO(30.0, 60.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarVocalizaciones(1L, dto);

        assertThat(result.vocalizaciones()).isNotNull();
        assertThat(m.getVocalizaciones()).isNotNull();
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

        var result = service.registrarVocalizaciones(1L, dto);

        assertThat(result.vocalizaciones()).isNotNull();
        assertThat(m.getVocalizaciones().getId()).isEqualTo(5L); // Reusado
        assertThat(m.getVocalizaciones().getMuestra1Khz()).isEqualTo(40.0);
        verify(repository).save(m);
    }

    @Test
    void deberia_registrarTresCamaras_cuandoModeloExiste() {
        var m = buildBaseModelo();
        m.setTresCamaras(null);
        var dto = new TresCamarasDTO(10.0, 5.0, 8.0, 4.0, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(repository.save(m)).thenReturn(m);

        var result = service.registrarTresCamaras(1L, dto);

        assertThat(result.tresCamaras()).isNotNull();
        assertThat(m.getTresCamaras()).isNotNull();
        assertThat(m.getTresCamaras().getM1TiempoRatonNovedad()).isEqualTo(10.0);
        verify(repository).save(m);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoModeloNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Modelo animal con id 99 no existe");
    }

    private ModeloAnimal buildBaseModelo() {
        var m = new ModeloAnimal();
        m.setId(1L);
        m.setIdentificador("M-1");
        m.setActivo(true);
        m.setFechaNacimiento(LocalDate.now(FIXED_CLOCK));
        m.setSexo(SexoRaton.MACHO);
        m.setFechaDia1Inoculacion(LocalDate.now(FIXED_CLOCK));

        var pool = new Pool();
        pool.setId(10L);
        pool.setRango(1);
        m.setPool(pool);

        var camada = new Camada();
        camada.setId(20L);
        camada.setNombre("C-1");
        m.setCamada(camada);

        return m;
    }
}
