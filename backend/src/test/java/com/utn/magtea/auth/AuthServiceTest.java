package com.utn.magtea.auth;

import com.utn.magtea.profesional.ProfesionalResponseDTO;
import com.utn.magtea.profesional.ProfesionalService;
import com.utn.magtea.profesional.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private ProfesionalService profesionalService;

    @InjectMocks private AuthService service;

    @Test
    void deberia_retornarLoginResponse_cuandoCredencialesValidas() {
        var request = new LoginRequest("ana@test.com", "pass1234");
        var profesionalDTO = new ProfesionalResponseDTO(1L, "Ana", "García", "ana@test.com", null, Role.CUERPO_MEDICO, true, null);

        when(profesionalService.findByEmail("ana@test.com")).thenReturn(profesionalDTO);
        when(jwtUtil.generateToken("ana@test.com", "CUERPO_MEDICO")).thenReturn("jwt-token");

        var result = service.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("ana@test.com");
        assertThat(result.role()).isEqualTo("CUERPO_MEDICO");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void deberia_lanzarExcepcion_cuandoAutenticacionFalla() {
        var request = new LoginRequest("ana@test.com", "wrong-pass");
        doThrow(new BadCredentialsException("Credenciales incorrectas"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(profesionalService, never()).findByEmail(any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
