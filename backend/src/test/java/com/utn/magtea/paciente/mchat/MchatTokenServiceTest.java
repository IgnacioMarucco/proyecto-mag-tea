package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MchatTokenServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-01T12:00:00Z"), ZoneId.of("UTC"));

    @Mock private PacienteRepository repository;
    @Mock private Clock clock;

    @InjectMocks private MchatTokenService service;

    // ── validarToken ─────────────────────────────────────────────────────────────

    @Test
    void deberia_validarToken_cuandoTokenValido() {
        var paciente = buildPaciente(1L);
        paciente.setMchatToken("token-valido");
        paciente.setMchatTokenExpiry(LocalDateTime.now(FIXED_CLOCK).plusDays(10));

        when(repository.findByMchatToken("token-valido")).thenReturn(Optional.of(paciente));
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        var result = service.validarToken("token-valido");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nombreNino()).isEqualTo("Juan");
        assertThat(result.apellidoNino()).isEqualTo("Pérez");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTokenNoExiste() {
        when(repository.findByMchatToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validarToken("token-inexistente"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("El enlace no es válido o ha expirado");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTokenExpirado() {
        var paciente = buildPaciente(1L);
        paciente.setMchatToken("token-expirado");
        paciente.setMchatTokenExpiry(LocalDateTime.now(FIXED_CLOCK).minusDays(1));

        when(repository.findByMchatToken("token-expirado")).thenReturn(Optional.of(paciente));
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        assertThatThrownBy(() -> service.validarToken("token-expirado"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoTokenSinExpiry() {
        var paciente = buildPaciente(1L);
        paciente.setMchatToken("token-sin-expiry");
        paciente.setMchatTokenExpiry(null);

        when(repository.findByMchatToken("token-sin-expiry")).thenReturn(Optional.of(paciente));

        // Cuando mchatTokenExpiry es null, el filter descarta el paciente
        assertThatThrownBy(() -> service.validarToken("token-sin-expiry"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── generarToken ─────────────────────────────────────────────────────────────

    @Test
    void deberia_generarToken_cuandoPacienteValido() {
        var paciente = buildPaciente(1L);
        ReflectionTestUtils.setField(service, "tokenExpiryDays", 30);

        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        service.generarToken(paciente);

        assertThat(paciente.getMchatToken()).isNotNull().hasSize(36); // UUID format
        assertThat(paciente.getMchatTokenExpiry()).isAfter(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    void deberia_generarTokenConExpiryConfigurable() {
        var paciente = buildPaciente(2L);
        ReflectionTestUtils.setField(service, "tokenExpiryDays", 7);

        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        service.generarToken(paciente);

        LocalDateTime esperado = LocalDateTime.now(FIXED_CLOCK).plusDays(7);
        assertThat(paciente.getMchatTokenExpiry()).isEqualTo(esperado);
    }

    // ── verificarActivo ───────────────────────────────────────────────────────────

    @Test
    void deberia_verificarActivo_cuandoPacienteExisteYActivo() {
        var paciente = buildPaciente(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatNoException().isThrownBy(() -> service.verificarActivo(1L));
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPacienteNoExisteAlVerificar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarActivo(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoPacienteInactivo() {
        var paciente = buildPaciente(1L);
        paciente.setActivo(false);

        when(repository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> service.verificarActivo(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private Paciente buildPaciente(Long id) {
        var p = new Paciente();
        p.setId(id);
        p.setActivo(true);
        p.setNombreNino("Juan");
        p.setApellidoNino("Pérez");
        return p;
    }
}
