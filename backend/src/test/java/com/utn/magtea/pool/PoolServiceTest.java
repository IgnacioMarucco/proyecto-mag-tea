package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private SueroRepository sueroRepository;
    @Mock private CajaRepository cajaRepository;

    @InjectMocks private PoolService service;

    @Test
    void deberia_crearPool_cuandoDatosValidos() {
        var sueroAport1 = new SueroAportDTO(1L, 0.15);
        var sueroAport2 = new SueroAportDTO(2L, 0.15);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport1, sueroAport2));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero1 = new Suero();
        suero1.setId(1L);
        suero1.setActivo(true);
        suero1.setRango(1);
        suero1.setCantidadTotal(1.0);
        suero1.setCantidadUsada(0.0);

        var suero2 = new Suero();
        suero2.setId(2L);
        suero2.setActivo(true);
        suero2.setRango(1);
        suero2.setCantidadTotal(1.0);
        suero2.setCantidadUsada(0.0);

        var response = new PoolResponseDTO(
                1L, 1L, "T1", dto.fechaCreacion(), 1, 0.3,
                0.0, 0.3, List.of(1L, 2L), true, null
        );

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero1));
        when(sueroRepository.findById(2L)).thenReturn(Optional.of(suero2));
        when(repository.save(any(Pool.class))).thenAnswer(inv -> {
            Pool p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(mapper.toDTO(any(Pool.class))).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.rango()).isEqualTo(1);
        assertThat(result.cantidadTotal()).isEqualTo(0.3);

        verify(sueroRepository).save(suero1);
        verify(sueroRepository).save(suero2);
        verify(repository).save(any(Pool.class));
    }

    @Test
    void deberia_incrementarCantidadUsada_cuandoCrearPool() {
        var sueroAport = new SueroAportDTO(1L, 0.3);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setId(1L);
        suero.setActivo(true);
        suero.setRango(1);
        suero.setCantidadTotal(1.0);
        suero.setCantidadUsada(0.1);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero));
        when(repository.save(any(Pool.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(dto);

        assertThat(suero.getCantidadUsada()).isEqualTo(0.4);
        verify(sueroRepository).save(suero);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSueroControl() {
        var sueroAport = new SueroAportDTO(1L, 0.3);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setId(1L);
        suero.setActivo(true);
        suero.setRango(0); // CONTROL

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Los sueros caso control (rango 0) no pueden formar un pool");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSuerosDeDistintoRango() {
        var sueroAport1 = new SueroAportDTO(1L, 0.15);
        var sueroAport2 = new SueroAportDTO(2L, 0.15);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport1, sueroAport2));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero1 = new Suero();
        suero1.setId(1L);
        suero1.setActivo(true);
        suero1.setRango(1);

        var suero2 = new Suero();
        suero2.setId(2L);
        suero2.setActivo(true);
        suero2.setRango(2); // Diferente

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero1));
        when(sueroRepository.findById(2L)).thenReturn(Optional.of(suero2));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Todos los sueros del pool deben ser del mismo rango");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoCantidadTotalMenorA200uL() {
        var sueroAport = new SueroAportDTO(1L, 0.1); // < 0.2
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setId(1L);
        suero.setActivo(true);
        suero.setRango(2);
        suero.setCantidadTotal(1.0);
        suero.setCantidadUsada(0.0);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El pool debe tener al menos 0.2 mL");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoSueroSinVolumenSuficiente() {
        var sueroAport = new SueroAportDTO(1L, 0.5);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setId(1L);
        suero.setActivo(true);
        suero.setRango(1);
        suero.setCantidadTotal(0.5);
        suero.setCantidadUsada(0.4); // disponible = 0.1, solicitado = 0.5

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(1L)).thenReturn(Optional.of(suero));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no tiene suficiente volumen disponible");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoSueroNoExiste() {
        var sueroAport = new SueroAportDTO(99L, 0.3);
        var dto = new PoolCreateDTO(1L, "T1", LocalDate.now(), List.of(sueroAport));

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Suero con id 99 no existe");
    }

    @Test
    void deberia_darDeBajaPool_cuandoExiste() {
        var pool = new Pool();
        pool.setId(1L);
        pool.setActivo(true);

        when(repository.findById(1L)).thenReturn(Optional.of(pool));
        when(repository.save(pool)).thenReturn(pool);

        service.delete(1L);

        assertThat(pool.isActivo()).isFalse();
        verify(repository).save(pool);
    }
}
