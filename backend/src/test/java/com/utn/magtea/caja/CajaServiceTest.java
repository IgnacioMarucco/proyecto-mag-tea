package com.utn.magtea.caja;

import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.suero.SueroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Ver PacienteServiceTest.java como referencia de estilo y estructura.
@ExtendWith(MockitoExtension.class)
class CajaServiceTest {

    @Mock private CajaRepository repository;
    @Mock private CajaMapper mapper;
    @Mock private SueroRepository sueroRepository;
    @Mock private PoolRepository poolRepository;

    @InjectMocks private CajaService service;

    @Test
    void deberia_crearCaja_cuandoDatosValidos() {
        var dto = new CajaCreateDTO("A", 1, 1);
        var caja = new Caja();
        caja.setFreezer("A");
        caja.setCajon(1);
        caja.setNumero(1);
        caja.setActivo(true);
        var response = new CajaResponseDTO(1L, "A", 1, 1, true, null);

        when(repository.existsByFreezerAndCajonAndNumeroAndActivoTrue("A", 1, 1)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(caja);
        when(repository.save(caja)).thenReturn(caja);
        when(mapper.toDTO(caja)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.freezer()).isEqualTo("A");
        verify(repository).save(caja);
    }

    @Test
    void deberia_lanzarDuplicateResourceException_cuandoMismaCombinacionFreezerCajonNumero() {
        var dto = new CajaCreateDTO("A", 1, 1);
        when(repository.existsByFreezerAndCajonAndNumeroAndActivoTrue("A", 1, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una caja activa en freezer A, cajón 1, número 1");
    }

    @Test
    void deberia_actualizarCaja_cuandoNuevaCombinacionNoExiste() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setFreezer("A");
        caja.setCajon(1);
        caja.setNumero(1);
        caja.setActivo(true);

        var dto = new CajaCreateDTO("B", 2, 2);
        var response = new CajaResponseDTO(1L, "B", 2, 2, true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(repository.existsByFreezerAndCajonAndNumeroAndActivoTrueAndIdNot("B", 2, 2, 1L)).thenReturn(false);
        when(repository.save(caja)).thenReturn(caja);
        when(mapper.toDTO(caja)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.freezer()).isEqualTo("B");
        assertThat(caja.getFreezer()).isEqualTo("B");
        assertThat(caja.getCajon()).isEqualTo(2);
        assertThat(caja.getNumero()).isEqualTo(2);
    }

    @Test
    void deberia_lanzarDuplicateResourceException_cuandoUpdateColisionaConOtraCaja() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setFreezer("A");
        caja.setCajon(1);
        caja.setNumero(1);
        caja.setActivo(true);

        var dto = new CajaCreateDTO("B", 2, 2);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(repository.existsByFreezerAndCajonAndNumeroAndActivoTrueAndIdNot("B", 2, 2, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una caja activa en freezer B, cajón 2, número 2");
    }

    @Test
    void deberia_darDeBaja_cuandoExiste() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(repository.save(caja)).thenReturn(caja);

        service.delete(1L);

        assertThat(caja.isActivo()).isFalse();
        verify(repository).save(caja);
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Caja con id 99 no existe");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCajaInactiva() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Caja con id 1 no existe");
    }

    @Test
    void deberia_devolverOcupacionVacia_cuandoCajaSinSuerosNiPools() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findByCajaIdAndActivoTrue(1L)).thenReturn(List.of());
        when(poolRepository.findByCajaIdAndActivoTrue(1L)).thenReturn(List.of());

        var result = service.getOcupacion(1L);

        assertThat(result.ocupadas()).isEmpty();
    }

    @Test
    void deberia_devolverTubosCombinados_cuandoExistenSuerosYPools() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero1 = new com.utn.magtea.suero.Suero();
        suero1.setTubos("A1, A2 ");
        var suero2 = new com.utn.magtea.suero.Suero();
        suero2.setTubos("A1,A3");

        var pool1 = new com.utn.magtea.pool.Pool();
        pool1.setTubos("B3");

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(sueroRepository.findByCajaIdAndActivoTrue(1L)).thenReturn(List.of(suero1, suero2));
        when(poolRepository.findByCajaIdAndActivoTrue(1L)).thenReturn(List.of(pool1));

        var result = service.getOcupacion(1L);

        assertThat(result.ocupadas()).containsExactly("A1", "A2", "A3", "B3");
    }
}
