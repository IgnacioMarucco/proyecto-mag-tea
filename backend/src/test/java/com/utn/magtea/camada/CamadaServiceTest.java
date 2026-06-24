package com.utn.magtea.camada;

import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamadaServiceTest {

    @Mock private CamadaRepository repository;
    @Mock private CamadaMapper mapper;
    @Mock private ModeloAnimalRepository modeloAnimalRepository;

    @InjectMocks private CamadaService service;

    @Test
    void deberia_listarCamadas_cuandoExisten() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setNombre("Camada-A");
        camada.setActivo(true);

        var listDTO = new CamadaListDTO(1L, "Camada-A", null);
        var page = new PageImpl<>(List.of(camada));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toListDTO(camada)).thenReturn(listDTO);

        var result = service.findAll(0, 10, null, "nombre", "asc");

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().nombre()).isEqualTo("Camada-A");
    }

    @Test
    void deberia_listarCamadas_cuandoBusquedaConQ() {
        var page = new PageImpl<Camada>(List.of());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = service.findAll(0, 10, "beta", "nombre", "asc");

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }

    @Test
    void deberia_obtenerCamada_cuandoExiste() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setNombre("Camada-A");
        camada.setActivo(true);

        var response = new CamadaResponseDTO(1L, "Camada-A", null, true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(camada));
        when(mapper.toDTO(camada)).thenReturn(response);

        var result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nombre()).isEqualTo("Camada-A");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 99 no existe");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaInactiva() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setNombre("Camada-A");
        camada.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(camada));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 1 no existe");
    }

    @Test
    void deberia_crearCamada_cuandoDatosValidos() {
        var dto = new CamadaCreateDTO("Camada-B", LocalDate.of(2026, 1, 15));
        var camada = new Camada();
        camada.setId(2L);
        camada.setNombre("Camada-B");
        camada.setActivo(true);
        var response = new CamadaResponseDTO(2L, "Camada-B", null, true, null);

        when(repository.existsByNombreAndActivoTrue("Camada-B")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(camada);
        when(repository.save(camada)).thenReturn(camada);
        when(mapper.toDTO(camada)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.nombre()).isEqualTo("Camada-B");
        verify(repository).save(camada);
    }

    @Test
    void deberia_lanzarDuplicateResourceException_cuandoNombreDuplicadoAlCrear() {
        var dto = new CamadaCreateDTO("Camada-A", LocalDate.of(2026, 1, 15));
        when(repository.existsByNombreAndActivoTrue("Camada-A")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una camada activa con el nombre \"Camada-A\"");
    }

    @Test
    void deberia_actualizarCamada_cuandoDatosValidos() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setNombre("Camada-A");
        camada.setActivo(true);

        var dto = new CamadaCreateDTO("Camada-A-Editada", LocalDate.of(2026, 2, 10));
        var response = new CamadaResponseDTO(1L, "Camada-A-Editada", LocalDate.of(2026, 2, 10), true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(camada));
        when(repository.existsByNombreAndActivoTrueAndIdNot("Camada-A-Editada", 1L)).thenReturn(false);
        when(repository.save(camada)).thenReturn(camada);
        when(mapper.toDTO(camada)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result.nombre()).isEqualTo("Camada-A-Editada");
        assertThat(camada.getNombre()).isEqualTo("Camada-A-Editada");
        verify(repository).save(camada);
    }

    @Test
    void deberia_lanzarDuplicateResourceException_cuandoNombreDuplicadoAlActualizar() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setNombre("Camada-A");
        camada.setActivo(true);

        var dto = new CamadaCreateDTO("Camada-B", LocalDate.of(2026, 1, 15));

        when(repository.findById(1L)).thenReturn(Optional.of(camada));
        when(repository.existsByNombreAndActivoTrueAndIdNot("Camada-B", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una camada activa con el nombre \"Camada-B\"");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaNoExisteAlActualizar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new CamadaCreateDTO("Camada-X", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 99 no existe");
    }

    @Test
    void deberia_eliminarCamada_cuandoExiste() {
        var camada = new Camada();
        camada.setId(1L);
        camada.setActivo(true);

        when(repository.findById(1L)).thenReturn(Optional.of(camada));
        when(modeloAnimalRepository.existsByCamada_IdAndActivoTrue(1L)).thenReturn(false);
        when(repository.save(camada)).thenReturn(camada);

        service.delete(1L);

        assertThat(camada.isActivo()).isFalse();
        verify(repository).save(argThat(c -> !c.isActivo()));
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoCamadaNoExisteAlEliminar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Camada con id 99 no existe");
    }
}
