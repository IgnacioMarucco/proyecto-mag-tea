package com.utn.magtea.caja;

import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CajaServiceTest {

    @Mock private CajaRepository repository;
    @Mock private CajaMapper mapper;
    @Mock private TuboRepository tuboRepository;

    @InjectMocks private CajaService service;

    @Test
    void deberia_listarCajas_cuandoExisten() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setFreezer("A");
        caja.setCajon(1);
        caja.setNumero(1);
        caja.setActivo(true);

        var listDTO = new CajaListDTO(1L, "A", 1, 1);
        var page = new PageImpl<>(List.of(caja));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toListDTO(caja)).thenReturn(listDTO);

        var result = service.findAll(0, 10, null, null, "freezer", "asc");

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().freezer()).isEqualTo("A");
    }

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
        when(tuboRepository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of());
        when(tuboRepository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        var result = service.getOcupacion(1L, null);

        assertThat(result.ocupadas()).isEmpty();
    }

    @Test
    void deberia_devolverTubosCombinados_cuandoExistenSuerosYPools() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var sueroEntidad = new com.utn.magtea.suero.Suero();
        sueroEntidad.setId(1L);
        sueroEntidad.setActivo(true);

        var t1 = new Tubo();
        t1.setSuero(sueroEntidad);
        t1.setPosicion("A1");
        t1.setCantidadInicial(1.0);
        t1.setCantidadUsada(0.0);

        var t2 = new Tubo();
        t2.setSuero(sueroEntidad);
        t2.setPosicion("A2");
        t2.setCantidadInicial(1.0);
        t2.setCantidadUsada(0.0);

        var poolEntidad = new com.utn.magtea.pool.Pool();
        poolEntidad.setId(1L);
        poolEntidad.setActivo(true);

        var t3 = new Tubo();
        t3.setPool(poolEntidad);
        t3.setPosicion("B3");
        t3.setCantidadInicial(1.0);
        t3.setCantidadUsada(0.0);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(t1, t2));
        when(tuboRepository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of(t3));

        var result = service.getOcupacion(1L, null);

        assertThat(result.ocupadas()).containsExactlyInAnyOrder("A1", "A2", "B3");
    }

    @Test
    void deberia_excluirSueroIndicado_cuandoGetOcupacionConExcludeId() {
        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var sueroExcluido = new com.utn.magtea.suero.Suero();
        sueroExcluido.setId(5L);
        sueroExcluido.setActivo(true);

        var tuboExcluido = new Tubo();
        tuboExcluido.setSuero(sueroExcluido);
        tuboExcluido.setPosicion("C1");
        tuboExcluido.setCantidadInicial(1.0);
        tuboExcluido.setCantidadUsada(0.0);

        when(repository.findById(1L)).thenReturn(Optional.of(caja));
        when(tuboRepository.findByCajaIdAndSueroActivoTrue(1L)).thenReturn(List.of(tuboExcluido));
        when(tuboRepository.findByCajaIdAndPoolActivoTrue(1L)).thenReturn(List.of());

        // Excluir sueroId=5 → C1 no debe aparecer
        var result = service.getOcupacion(1L, 5L);

        assertThat(result.ocupadas()).isEmpty();
    }
}
