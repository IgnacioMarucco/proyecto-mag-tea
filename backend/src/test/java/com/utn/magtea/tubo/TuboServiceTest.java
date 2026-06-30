package com.utn.magtea.tubo;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.suero.Suero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TuboServiceTest {

    @Mock private TuboRepository repository;

    @InjectMocks private TuboService service;

    // ── vaciar ───────────────────────────────────────────────────────────────────

    @Test
    void deberia_vaciarTubo_cuandoTieneCantidadRestante() {
        var tubo = buildTubo(1L, BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.3));
        var req = new VaciarTuboRequest(MotivoVaciado.CONTAMINADO, "Nota de vaciado");

        when(repository.findById(1L)).thenReturn(Optional.of(tubo));
        when(repository.save(tubo)).thenReturn(tubo);

        service.vaciar(1L, req);

        assertThat(tubo.getCantidadUsada()).isEqualByComparingTo("1.0");
        assertThat(tubo.getMotivoVaciado()).isEqualTo(MotivoVaciado.CONTAMINADO);
        assertThat(tubo.getNotasVaciado()).isEqualTo("Nota de vaciado");
        assertThat(tubo.getPosicion()).isNull();
        verify(repository).save(tubo);
    }

    @Test
    void deberia_vaciarTubo_cuandoYaEstaAgotado() {
        var tubo = buildTubo(1L, BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0));
        var req = new VaciarTuboRequest(MotivoVaciado.CONSUMIDO, null);

        when(repository.findById(1L)).thenReturn(Optional.of(tubo));
        when(repository.save(tubo)).thenReturn(tubo);

        service.vaciar(1L, req);

        // El tubo ya está agotado: cantidadUsada no debe cambiar (la condición > 0 no se cumple)
        assertThat(tubo.getCantidadUsada()).isEqualByComparingTo("1.0");
        assertThat(tubo.getMotivoVaciado()).isEqualTo(MotivoVaciado.CONSUMIDO);
        verify(repository).save(tubo);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTuboNoExisteAlVaciar() {
        var req = new VaciarTuboRequest(MotivoVaciado.CONTAMINADO, null);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vaciar(99L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tubo con id 99 no existe");
    }

    // ── validarPosicionesSinConflicto ────────────────────────────────────────────

    @Test
    void deberia_validarSinConflicto_cuandoCajaVacia() {
        var nuevas = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(0.5)));

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of());
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        assertThatNoException().isThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, null, Set.of()));
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoConflictoConTuboDeSuero() {
        var nuevas = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(0.5)));

        var suero = new Suero();
        suero.setId(10L);
        suero.setActivo(true);

        var tuboOcupado = buildTubo(5L, BigDecimal.valueOf(1.0), BigDecimal.ZERO);
        tuboOcupado.setPosicion("A1");
        tuboOcupado.setSuero(suero);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(tuboOcupado));
        lenient().when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        assertThatThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, null, Set.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("A1")
                .hasMessageContaining("suero");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoConflictoConTuboDePool() {
        var nuevas = List.of(new TuboInputDTO("B3", BigDecimal.valueOf(0.3)));

        var pool = new Pool();
        pool.setId(20L);
        pool.setActivo(true);

        var tuboPool = buildTubo(6L, BigDecimal.valueOf(0.5), BigDecimal.ZERO);
        tuboPool.setPosicion("B3");
        tuboPool.setPool(pool);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of());
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of(tuboPool));

        assertThatThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, null, Set.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("B3")
                .hasMessageContaining("pool");
    }

    @Test
    void deberia_ignorarTuboExcluido_cuandoSueroIdCoincide() {
        var nuevas = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(0.5)));

        var suero = new Suero();
        suero.setId(10L);
        suero.setActivo(true);

        var tuboExcluido = buildTubo(5L, BigDecimal.valueOf(1.0), BigDecimal.ZERO);
        tuboExcluido.setPosicion("A1");
        tuboExcluido.setSuero(suero);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(tuboExcluido));
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        // Al excluir el sueroId=10, A1 queda libre
        assertThatNoException().isThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, 10L, null, Set.of()));
    }

    @Test
    void deberia_ignorarTuboAgotado_cuandoExcluirTuboIds() {
        var nuevas = List.of(new TuboInputDTO("A2", BigDecimal.valueOf(0.5)));

        var suero = new Suero();
        suero.setId(10L);
        suero.setActivo(true);

        // Tubo en A2 pero ya agotado y su ID está en excluirTuboIds
        var tuboAgotado = buildTubo(7L, BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0));
        tuboAgotado.setPosicion("A2");
        tuboAgotado.setSuero(suero);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(tuboAgotado));
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        // Agotado → getCantidadRestante() = 0 → no se toma como ocupado
        assertThatNoException().isThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, null, Set.of(7L)));
    }

    @Test
    void deberia_ignorarTuboPool_cuandoPoolIdCoincide() {
        var nuevas = List.of(new TuboInputDTO("C1", BigDecimal.valueOf(0.3)));

        var pool = new Pool();
        pool.setId(20L);
        pool.setActivo(true);

        var tuboPool = buildTubo(8L, BigDecimal.valueOf(0.5), BigDecimal.ZERO);
        tuboPool.setPosicion("C1");
        tuboPool.setPool(pool);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of());
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of(tuboPool));

        // Al excluir poolId=20, C1 queda libre
        assertThatNoException().isThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, 20L, Set.of()));
    }

    @Test
    void deberia_ignorarTuboSinPosicion_cuandoEstaVacio() {
        var nuevas = List.of(new TuboInputDTO("A1", BigDecimal.valueOf(0.5)));

        var suero = new Suero();
        suero.setId(10L);
        suero.setActivo(true);

        // Tubo sin posición (ya fue vaciado)
        var tuboSinPos = buildTubo(9L, BigDecimal.valueOf(1.0), BigDecimal.ZERO);
        tuboSinPos.setPosicion(null);
        tuboSinPos.setSuero(suero);

        when(repository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(tuboSinPos));
        when(repository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        assertThatNoException().isThrownBy(() ->
            service.validarPosicionesSinConflicto(1L, nuevas, null, null, Set.of()));
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private Tubo buildTubo(Long id, BigDecimal cantidadInicial, BigDecimal cantidadUsada) {
        var t = new Tubo();
        t.setId(id);
        t.setCantidadInicial(cantidadInicial);
        t.setCantidadUsada(cantidadUsada);
        return t;
    }
}
