package com.utn.magtea.formulariointeres;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormularioInteresServiceTest {

    @Mock private FormularioInteresRepository repository;
    @Mock private FormularioInteresMapper mapper;

    @InjectMocks private FormularioInteresService service;

    @Test
    void deberia_crearFormulario_cuandoDatosValidos() {
        var dto = new FormularioInteresCreateDTO("García", "Ana", "ana@test.com", null, "Niño", "Nombre", null, null, null, null);
        var entidad = new FormularioInteres();
        var response = buildResponse(1L, EstadoFormulario.PENDIENTE);

        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isEqualTo(response);
        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.PENDIENTE);
        assertThat(entidad.getFechaContacto()).isNotNull();
    }

    @Test
    void deberia_retornarFormulario_cuandoIdExiste() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);
        var response = buildResponse(1L, EstadoFormulario.PENDIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(mapper.toDTO(entidad)).thenReturn(response);

        assertThat(service.findById(1L)).isEqualTo(response);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deberia_retornarFormularios_cuandoListar() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);
        var response = buildResponse(1L, EstadoFormulario.PENDIENTE);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad)));
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.findAll(0, 20, null, null, "fechaContacto", "desc");

        assertThat(result.content()).hasSize(1).contains(response);
    }

    @Test
    void deberia_actualizarFormulario_cuandoEstadoPendiente() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);
        var dto = new FormularioInteresCreateDTO("López", "María", "maria@test.com", "1234", "Niño", "Pepe", null, null, null, "Lunes");
        var response = buildResponse(1L, EstadoFormulario.PENDIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        service.update(1L, dto);

        assertThat(entidad.getApellidoTutor()).isEqualTo("López");
        assertThat(entidad.getDiasDisponibles()).isEqualTo("Lunes");
    }

    @Test
    void deberia_lanzarExcepcion_alActualizar_cuandoEstadoAdmitido() {
        var dto = new FormularioInteresCreateDTO("García", "Ana", "ana@test.com", null, "Niño", "Nombre", null, null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(formularioActivo(EstadoFormulario.ADMITIDO)));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMITIDO");
    }

    @Test
    void deberia_lanzarExcepcion_alActualizar_cuandoEstadoDescartado() {
        var dto = new FormularioInteresCreateDTO("García", "Ana", "ana@test.com", null, "Niño", "Nombre", null, null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(formularioActivo(EstadoFormulario.DESCARTADO)));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DESCARTADO");
    }

    @Test
    void deberia_cambiarEstado_cuandoPendienteAContactado() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L, EstadoFormulario.CONTACTADO));

        service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.CONTACTADO));

        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.CONTACTADO);
    }

    @Test
    void deberia_cambiarEstado_cuandoContactadoAPendiente() {
        var entidad = formularioActivo(EstadoFormulario.CONTACTADO);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L, EstadoFormulario.PENDIENTE));

        service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.PENDIENTE));

        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.PENDIENTE);
    }

    @Test
    void deberia_cambiarEstado_cuandoContactadoADescartado() {
        var entidad = formularioActivo(EstadoFormulario.CONTACTADO);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L, EstadoFormulario.DESCARTADO));

        service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.DESCARTADO));

        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.DESCARTADO);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoTransicionInvalida_pendienteAAdmitido() {
        when(repository.findById(1L)).thenReturn(Optional.of(formularioActivo(EstadoFormulario.PENDIENTE)));

        assertThatThrownBy(() -> service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.ADMITIDO)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDIENTE")
                .hasMessageContaining("ADMITIDO");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoTransicionInvalida_admitidoACualquiera() {
        when(repository.findById(1L)).thenReturn(Optional.of(formularioActivo(EstadoFormulario.ADMITIDO)));

        assertThatThrownBy(() -> service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.CONTACTADO)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deberia_admitir_cuandoEstadoContactado() {
        var entidad = formularioActivo(EstadoFormulario.CONTACTADO);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);

        var result = service.admitir(1L);

        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.ADMITIDO);
        assertThat(result).isSameAs(entidad);
    }

    @Test
    void deberia_lanzarExcepcion_alAdmitir_cuandoEstadoPendiente() {
        when(repository.findById(1L)).thenReturn(Optional.of(formularioActivo(EstadoFormulario.PENDIENTE)));

        assertThatThrownBy(() -> service.admitir(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CONTACTADO");
    }

    @Test
    void deberia_marcarInactivo_cuandoBajaLogica() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        service.delete(1L);

        assertThat(entidad.isActivo()).isFalse();
        verify(repository).save(entidad);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoBajaLogicaYNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deberia_cambiarEstado_cuandoPendienteADescartado() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(buildResponse(1L, EstadoFormulario.DESCARTADO));

        service.cambiarEstado(1L, new EstadoUpdateDTO(EstadoFormulario.DESCARTADO));

        assertThat(entidad.getEstado()).isEqualTo(EstadoFormulario.DESCARTADO);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoFormularioInactivo() {
        var entidad = formularioActivo(EstadoFormulario.PENDIENTE);
        entidad.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deberia_listarFormularios_cuandoFiltroEstados() {
        var entidad = formularioActivo(EstadoFormulario.CONTACTADO);
        var response = buildResponse(1L, EstadoFormulario.CONTACTADO);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad)));
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.findAll(0, 20, null, List.of(EstadoFormulario.CONTACTADO), "fechaContacto", "asc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().estado()).isEqualTo(EstadoFormulario.CONTACTADO);
    }

    // Helpers
    private FormularioInteres formularioActivo(EstadoFormulario estado) {
        var f = new FormularioInteres();
        f.setActivo(true);
        f.setEstado(estado);
        return f;
    }

    private FormularioInteresResponseDTO buildResponse(Long id, EstadoFormulario estado) {
        return new FormularioInteresResponseDTO(id, null, "García", "Ana", "ana@test.com",
                null, "Niño", "Nombre", null, null, null, null, null, null, estado, true, null);
    }
}
