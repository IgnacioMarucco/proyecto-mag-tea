package com.utn.magtea.auth;

import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private ProfesionalRepository repository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void deberia_retornarUserDetails_cuandoEmailExisteYUsuarioActivo() {
        var profesional = new Profesional();
        profesional.setEmail("test@test.com");
        profesional.setPassword("hashed-password");
        profesional.setRole(Role.CUERPO_MEDICO);
        profesional.setActivo(true);

        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(profesional));

        UserDetails result = service.loadUserByUsername("test@test.com");

        assertThat(result.getUsername()).isEqualTo("test@test.com");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUERPO_MEDICO");
    }

    @Test
    void deberia_retornarUserDetailsDeshabilitado_cuandoUsuarioInactivo() {
        var profesional = new Profesional();
        profesional.setEmail("test@test.com");
        profesional.setPassword("hashed-password");
        profesional.setRole(Role.CUERPO_MEDICO);
        profesional.setActivo(false);

        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(profesional));

        UserDetails result = service.loadUserByUsername("test@test.com");

        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void deberia_lanzarUsernameNotFoundException_cuandoEmailNoExiste() {
        when(repository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("notfound@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Profesional no encontrado: notfound@test.com");
    }
}
