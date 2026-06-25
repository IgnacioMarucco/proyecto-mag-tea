package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboInputDTO;
import com.utn.magtea.tubo.TuboRepository;
import com.utn.magtea.tubo.TuboService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PoolServiceTest {

    @Mock private PoolRepository repository;
    @Mock private PoolMapper mapper;
    @Mock private TuboRepository tuboRepository;
    @Mock private PoolSueroAporteRepository poolSueroAporteRepository;
    @Mock private CajaRepository cajaRepository;
    @Mock private TuboService tuboService;

    @InjectMocks private PoolService service;

    // --- findAll ---

    @Test
    void deberia_listarPools_cuandoExisten() {
        var pool = buildPool(1L);
        var listDTO = new PoolListDTO(1L, "ABC123", 1, SueroUso.PROBLEMA, BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.3), LocalDate.now(), 0, "A-1-1", 0);
        var page = new PageImpl<>(List.of(pool));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toListDTO(pool)).thenReturn(listDTO);

        var result = service.findAll(0, 10, null, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void deberia_listarPools_cuandoFiltroRangosYUsos() {
        var page = new PageImpl<Pool>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, null, List.of(1, 2), List.of(SueroUso.PROBLEMA), "rango", "asc");

        assertThat(result.content()).isEmpty();
    }

    // --- findById ---

    @Test
    void deberia_obtenerPool_cuandoExiste() {
        var pool = buildPool(1L);
        var response = buildResponseDTO(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(mapper.toDTO(pool)).thenReturn(response);

        var result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPoolNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pool con id 99 no existe");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPoolInactivo() {
        var pool = buildPool(1L);
        pool.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pool con id 1 no existe");
    }

    // --- create ---

    @Test
    void deberia_crearPool_cuandoDatosValidos() {
        var aportes = List.of(new SueroTuboAporteInputDTO(1L, BigDecimal.valueOf(0.15)), new SueroTuboAporteInputDTO(2L, BigDecimal.valueOf(0.15)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.3)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);
        var st1 = buildSueroTubo(1L, 1, SueroUso.PROBLEMA, "A1", 1.0, 0.0);
        var st2 = buildSueroTubo(2L, 1, SueroUso.PROBLEMA, "A2", 1.0, 0.0);
        var pool = buildPool(1L);
        var response = buildResponseDTO(1L);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(1L)).thenReturn(Optional.of(st1));
        when(tuboRepository.findById(2L)).thenReturn(Optional.of(st2));
        when(repository.save(any(Pool.class))).thenReturn(pool);
        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(mapper.toDTO(pool)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Pool.class));
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSuerosDeDistintoRango() {
        var aportes = List.of(new SueroTuboAporteInputDTO(1L, BigDecimal.valueOf(0.15)), new SueroTuboAporteInputDTO(2L, BigDecimal.valueOf(0.15)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.3)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);
        var st1 = buildSueroTubo(1L, 1, SueroUso.PROBLEMA, "A1", 1.0, 0.0);
        var st2 = buildSueroTubo(2L, 2, SueroUso.PROBLEMA, "A2", 1.0, 0.0); // rango distinto

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(1L)).thenReturn(Optional.of(st1));
        when(tuboRepository.findById(2L)).thenReturn(Optional.of(st2));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Todos los tubos aportantes deben ser del mismo rango");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSuerosDeDistintoTipo() {
        var aportes = List.of(new SueroTuboAporteInputDTO(1L, BigDecimal.valueOf(0.15)), new SueroTuboAporteInputDTO(2L, BigDecimal.valueOf(0.15)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.3)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);
        var st1 = buildSueroTubo(1L, 1, SueroUso.PROBLEMA, "A1", 1.0, 0.0);
        var st2 = buildSueroTubo(2L, 1, SueroUso.CONTROL, "A2", 1.0, 0.0); // uso distinto

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(1L)).thenReturn(Optional.of(st1));
        when(tuboRepository.findById(2L)).thenReturn(Optional.of(st2));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Todos los tubos aportantes deben ser del mismo tipo");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoCantidadTotalMenorA200uL() {
        var aportes = List.of(new SueroTuboAporteInputDTO(1L, BigDecimal.valueOf(0.1)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.1)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);
        var st1 = buildSueroTubo(1L, 2, SueroUso.PROBLEMA, "A1", 1.0, 0.0);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(1L)).thenReturn(Optional.of(st1));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El pool debe tener al menos 0.20 mL");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSueroSinVolumenSuficiente() {
        var aportes = List.of(new SueroTuboAporteInputDTO(1L, BigDecimal.valueOf(0.5)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.5)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);
        var st = buildSueroTubo(1L, 1, SueroUso.PROBLEMA, "A1", 0.5, 0.4); // restante = 0.1

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(1L)).thenReturn(Optional.of(st));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no tiene suficiente volumen disponible");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTuboNoExiste() {
        var aportes = List.of(new SueroTuboAporteInputDTO(99L, BigDecimal.valueOf(0.3)));
        var tubos = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.3)));
        var dto = new PoolCreateDTO(1L, aportes, tubos);

        var caja = buildCaja(1L);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tubo con id 99 no existe");
    }

    // --- update ---

    @Test
    void deberia_actualizarPool_cuandoDatosValidos() {
        var pool = buildPool(1L);
        var caja = buildCaja(1L);
        var tubosInput = List.of(new TuboInputDTO("P1", BigDecimal.valueOf(0.3)));
        var dto = new PoolUpdateDTO(1L, LocalDate.now(), tubosInput);
        var response = buildResponseDTO(1L);

        var tuboExistente = new Tubo();
        tuboExistente.setTipo(TipoTubo.POOL);
        tuboExistente.setPosicion("P1");
        tuboExistente.setCantidadInicial(BigDecimal.valueOf(0.3));
        tuboExistente.setCantidadUsada(BigDecimal.ZERO);
        tuboExistente.setPool(pool);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findByPoolId(1L)).thenReturn(List.of(tuboExistente));
        when(repository.save(pool)).thenReturn(pool);
        when(mapper.toDTO(pool)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result).isNotNull();
        verify(repository).save(pool);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoTuboPoolConVolumenUsadoEliminado() {
        var pool = buildPool(1L);
        var caja = buildCaja(1L);
        // dto pide P2, pero P1 tiene volumen usado → debe fallar
        var dto = new PoolUpdateDTO(1L, LocalDate.now(), List.of(new TuboInputDTO("P2", BigDecimal.valueOf(0.3))));

        var tuboConUso = new Tubo();
        tuboConUso.setTipo(TipoTubo.POOL);
        tuboConUso.setPosicion("P1");
        tuboConUso.setCantidadInicial(BigDecimal.valueOf(0.3));
        tuboConUso.setCantidadUsada(BigDecimal.valueOf(0.1));
        tuboConUso.setPool(pool);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findByPoolId(1L)).thenReturn(List.of(tuboConUso));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("mL usados y no puede eliminarse");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPoolNoExisteAlActualizar() {
        var dto = new PoolUpdateDTO(1L, LocalDate.now(), List.of());

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pool con id 99 no existe");
    }

    // --- delete ---

    @Test
    void deberia_darDeBajaPool_cuandoExiste() {
        var pool = buildPool(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(repository.save(pool)).thenReturn(pool);

        service.delete(1L);

        assertThat(pool.isActivo()).isFalse();
        verify(repository).save(argThat(p -> !p.isActivo()));
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPoolNoExisteAlEliminar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pool con id 99 no existe");
    }

    // --- Helpers ---

    private Pool buildPool(Long id) {
        var pool = new Pool();
        pool.setId(id);
        pool.setActivo(true);
        pool.setRango(1);
        pool.setUso(SueroUso.PROBLEMA);
        return pool;
    }

    private Caja buildCaja(Long id) {
        var c = new Caja();
        c.setId(id);
        c.setActivo(true);
        return c;
    }

    private Tubo buildSueroTubo(Long id, int rango, SueroUso uso, String posicion,
                                 double cantidadInicial, double cantidadUsada) {
        var suero = new Suero();
        suero.setId(id);
        suero.setActivo(true);
        suero.setRango(rango);
        suero.setUso(uso);

        var t = new Tubo();
        t.setId(id);
        t.setTipo(TipoTubo.SUERO);
        t.setSuero(suero);
        t.setPosicion(posicion);
        t.setCantidadInicial(BigDecimal.valueOf(cantidadInicial));
        t.setCantidadUsada(BigDecimal.valueOf(cantidadUsada));
        return t;
    }

    private PoolResponseDTO buildResponseDTO(Long id) {
        return new PoolResponseDTO(id, "ABC123", 1L, "F1", 1, 1,
                List.of(), LocalDate.now(), 1, SueroUso.PROBLEMA, BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.3), List.of(), true, null);
    }
}
