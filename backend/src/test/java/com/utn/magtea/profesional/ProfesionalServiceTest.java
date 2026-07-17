package com.utn.magtea.profesional;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfesionalServiceTest {

    @Mock private ProfesionalRepository repository;
    @Mock private ProfesionalMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ProfesionalService service;

    @Test
    void deberia_crearProfesional_cuandoDatosValidos() {
        var dto = new ProfesionalCreateDTO("Ana", "García", "ana@test.com", "351-000-0000", "pass1234", Role.CUERPO_MEDICO);
        var entidad = new Profesional();
        var response = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", "351-000-0000", Role.CUERPO_MEDICO, true, null);

        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entidad);
        when(passwordEncoder.encode(dto.password())).thenReturn("hashed");
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isEqualTo(response);
        verify(passwordEncoder).encode("pass1234");
        verify(repository).save(entidad);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoEmailDuplicadoAlCrear() {
        var dto = new ProfesionalCreateDTO("Ana", "García", "ana@test.com", "351-000-0000", "pass1234", Role.CUERPO_MEDICO);
        when(repository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ana@test.com");
    }

    @Test
    void deberia_retornarProfesional_cuandoIdExiste() {
        var entidad = new Profesional();
        entidad.setActivo(true);
        var response = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", null, Role.CUERPO_MEDICO, true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.findById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void deberia_lanzarExcepcion_cuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deberia_retornarSoloProfesionalesActivos_cuandoListar() {
        var activo = new Profesional();
        activo.setActivo(true);
        var response = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", null, Role.CUERPO_MEDICO, true, null);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activo)));
        when(mapper.toDTO(activo)).thenReturn(response);

        var result = service.findAll(0, 20, null, null, "apellido", "asc");

        assertThat(result.content()).hasSize(1).contains(response);
    }

    @Test
    void deberia_marcarInactivo_cuandoBajaLogica() {
        var entidad = new Profesional();
        entidad.setActivo(true);
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
    void deberia_actualizarProfesional_cuandoDatosValidos() {
        var dto = new ProfesionalUpdateDTO("Ana", "López", "ana@test.com", "351-000-0000", Role.CUERPO_TECNICO, null);
        var entidad = new Profesional();
        entidad.setActivo(true);
        entidad.setEmail("ana@test.com");
        var response = new ProfesionalResponseDTO(1L, "Ana", "López", "ana@test.com", "351-000-0000", Role.CUERPO_TECNICO, true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.save(any())).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result).isEqualTo(response);
        assertThat(entidad.getRole()).isEqualTo(Role.CUERPO_TECNICO);
    }

    @Test
    void deberia_retornarProfesional_cuandoBuscarPorEmail() {
        var entidad = new Profesional();
        entidad.setActivo(true);
        entidad.setEmail("ana@test.com");
        var response = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", null, Role.CUERPO_MEDICO, true, null);

        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(entidad));
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.findByEmail("ana@test.com");

        assertThat(result).isEqualTo(response);
        assertThat(result.email()).isEqualTo("ana@test.com");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoEmailNoExisteEnBusqueda() {
        when(repository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("noexiste@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profesional no encontrado");
    }

    @Test
    void deberia_lanzarDuplicateException_cuandoUpdateConEmailDeOtroProfesional() {
        var dto = new ProfesionalUpdateDTO("Ana", "García", "nuevo@test.com", "351-000-0000", Role.CUERPO_MEDICO, null);
        var entidad = new Profesional();
        entidad.setActivo(true);
        entidad.setEmail("viejo@test.com"); // email distinto al del DTO

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(repository.existsByEmail("nuevo@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("nuevo@test.com");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoProfesionalInactivo() {
        var entidad = new Profesional();
        entidad.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void deberia_listarProfesionalesConFiltroRol() {
        var entidad = new Profesional();
        entidad.setActivo(true);
        entidad.setRole(Role.CUERPO_TECNICO);
        var response = new ProfesionalResponseDTO(1L, "Carlos", "López", "carlos@test.com", null, Role.CUERPO_TECNICO, true, null);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad)));
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.findAll(0, 20, null, Role.CUERPO_TECNICO, "apellido", "asc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().role()).isEqualTo(Role.CUERPO_TECNICO);
    }

    @Test
    void deberia_actualizarProfesionalConPassword_cuandoPasswordProporcionado() {
        var dto = new ProfesionalUpdateDTO("Ana", "López", "ana@test.com", "351-000-0000", Role.CUERPO_TECNICO, "nuevapass123");
        var entidad = new Profesional();
        entidad.setActivo(true);
        entidad.setEmail("ana@test.com");
        var response = new ProfesionalResponseDTO(1L, "Ana", "López", "ana@test.com", "351-000-0000", Role.CUERPO_TECNICO, true, null);

        when(repository.findById(1L)).thenReturn(Optional.of(entidad));
        when(passwordEncoder.encode("nuevapass123")).thenReturn("hashedNueva");
        when(repository.save(any())).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.update(1L, dto);

        assertThat(result).isEqualTo(response);
        verify(passwordEncoder).encode("nuevapass123");
    }

    @Test
    void deberia_actualizarPerfilPropio_cuandoDatosValidos() {
        var dto = new PerfilUpdateDTO("Ana", "García", "ana@test.com", "351-111-1111");
        var entidad = new Profesional();
        entidad.setEmail("ana@test.com");
        var response = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", "351-111-1111", Role.CUERPO_MEDICO, true, null);

        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(entidad));
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toDTO(entidad)).thenReturn(response);

        var result = service.updateSelf("ana@test.com", dto);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void deberia_lanzarDuplicateException_cuandoUpdateSelfConEmailDeOtroProfesional() {
        var dto = new PerfilUpdateDTO("Ana", "García", "otro@test.com", "351-111-1111");
        var entidad = new Profesional();
        entidad.setEmail("ana@test.com");

        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(entidad));
        when(repository.existsByEmail("otro@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateSelf("ana@test.com", dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("otro@test.com");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoUpdateSelfYProfesionalNoExiste() {
        var dto = new PerfilUpdateDTO("Ana", "García", "ana@test.com", "351-111-1111");
        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSelf("ana@test.com", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deberia_cambiarPassword_cuandoContrasenaActualCorrecta() {
        var dto = new CambiarPasswordDTO("passVieja", "passNueva");
        var entidad = new Profesional();
        entidad.setPassword("hashedVieja");

        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(entidad));
        when(passwordEncoder.matches("passVieja", "hashedVieja")).thenReturn(true);
        when(passwordEncoder.encode("passNueva")).thenReturn("hashedNueva");

        service.changePassword("ana@test.com", dto);

        verify(repository).save(entidad);
        assertThat(entidad.getPassword()).isEqualTo("hashedNueva");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoContrasenaActualIncorrecta() {
        var dto = new CambiarPasswordDTO("passIncorrecta", "passNueva");
        var entidad = new Profesional();
        entidad.setPassword("hashedVieja");

        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.of(entidad));
        when(passwordEncoder.matches("passIncorrecta", "hashedVieja")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword("ana@test.com", dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La contraseña actual es incorrecta");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoChangePasswordYProfesionalNoExiste() {
        var dto = new CambiarPasswordDTO("passVieja", "passNueva");
        when(repository.findByEmail("ana@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword("ana@test.com", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
