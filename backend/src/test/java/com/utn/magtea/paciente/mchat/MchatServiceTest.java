package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.EntityManager;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.mchat.MchatSeguimientoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MchatServiceTest {

    @Mock private ApplicationEventPublisher events;
    @Mock private MchatTokenService mchatTokenService;
    @Mock private MchatFamiliaRepository familiaRepository;
    @Mock private MchatFamiliaMapper mapper;
    @Mock private EntityManager em;

    @InjectMocks private MchatService service;

    // ─── validarToken ─────────────────────────────────────────────────────────

    @Test
    void deberia_validarToken_cuandoTokenValido_sinCompletar() {
        var info = new MchatInfoDTO(1L, "Nombre", "Niño");

        when(mchatTokenService.validarToken("token-abc")).thenReturn(info);
        when(familiaRepository.existsByPaciente_Id(1L)).thenReturn(false);

        var result = service.validarToken("token-abc");

        assertThat(result.nombreNino()).isEqualTo("Nombre");
        assertThat(result.apellidoNino()).isEqualTo("Niño");
        assertThat(result.yaCompletado()).isFalse();
    }

    @Test
    void deberia_validarToken_cuandoTokenValido_yaCompletado() {
        var info = new MchatInfoDTO(1L, "Nombre", "Niño");

        when(mchatTokenService.validarToken("token-abc")).thenReturn(info);
        when(familiaRepository.existsByPaciente_Id(1L)).thenReturn(true);

        assertThat(service.validarToken("token-abc").yaCompletado()).isTrue();
    }

    @Test
    void deberia_lanzarExcepcion_cuandoTokenInvalido() {
        when(mchatTokenService.validarToken("token-invalido"))
                .thenThrow(new ResourceNotFoundException("El enlace no es válido o ha expirado"));

        assertThatThrownBy(() -> service.validarToken("token-invalido"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── guardarRespuestas ────────────────────────────────────────────────────

    @Test
    void deberia_guardarRespuestas_cuandoPrimeraVez() {
        var info = new MchatInfoDTO(1L, "Nombre", "Niño");
        var dto = todosCorrectos();
        var pacienteMock = mock(Paciente.class);

        when(mchatTokenService.validarToken("token-abc")).thenReturn(info);
        when(familiaRepository.existsByPaciente_Id(1L)).thenReturn(false);
        when(em.getReference(Paciente.class, 1L)).thenReturn(pacienteMock);

        service.guardarRespuestas("token-abc", dto);

        var captor = ArgumentCaptor.forClass(MchatFamilia.class);
        verify(familiaRepository).save(captor.capture());
        assertThat(captor.getValue().getPaciente()).isSameAs(pacienteMock);
        assertThat(captor.getValue().getScoreTotal()).isEqualTo(0);
        assertThat(captor.getValue().getResultadoFinal()).isEqualTo(MchatResultadoFinal.NEGATIVA);
        verify(events).publishEvent(any(MchatEvents.MchatFamiliaGuardadaEvent.class));
    }

    @Test
    void deberia_lanzarExcepcion_alGuardar_cuandoYaCompletado() {
        var info = new MchatInfoDTO(1L, "Nombre", "Niño");

        when(mchatTokenService.validarToken("token-abc")).thenReturn(info);
        when(familiaRepository.existsByPaciente_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.guardarRespuestas("token-abc", todosCorrectos()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue completado");

        verify(familiaRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    // ─── getRespuestasByPaciente ──────────────────────────────────────────────

    @Test
    void deberia_retornarRespuestas_cuandoExisten() {
        var familia = new MchatFamilia();
        var response = mock(MchatFamiliaResponseDTO.class);

        when(familiaRepository.findByPaciente_Id(1L)).thenReturn(Optional.of(familia));
        when(mapper.toDTO(familia)).thenReturn(response);

        assertThat(service.getRespuestasByPaciente(1L)).isSameAs(response);
    }

    @Test
    void deberia_lanzarExcepcion_getRespuestas_cuandoNoExisten() {
        when(familiaRepository.findByPaciente_Id(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRespuestasByPaciente(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── upsertRespuestasByPaciente ───────────────────────────────────────────

    @Test
    void deberia_upsertRespuestas_cuandoEsPrimeraVez() {
        var dto = todosCorrectos();
        var response = mock(MchatFamiliaResponseDTO.class);
        var pacienteMock = mock(Paciente.class);

        when(familiaRepository.findByPaciente_Id(1L)).thenReturn(Optional.empty());
        when(em.getReference(Paciente.class, 1L)).thenReturn(pacienteMock);
        when(familiaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(MchatFamilia.class))).thenReturn(response);

        var result = service.upsertRespuestasByPaciente(1L, dto);

        assertThat(result).isSameAs(response);
        verify(mchatTokenService).verificarActivo(1L);
        verify(events).publishEvent(any(MchatEvents.MchatFamiliaActualizadaEvent.class));
    }

    @Test
    void deberia_upsertRespuestas_cuandoYaExistia() {
        var dto = todosCorrectos();
        var familiaExistente = new MchatFamilia();
        var response = mock(MchatFamiliaResponseDTO.class);

        when(familiaRepository.findByPaciente_Id(1L)).thenReturn(Optional.of(familiaExistente));
        when(familiaRepository.save(familiaExistente)).thenReturn(familiaExistente);
        when(mapper.toDTO(familiaExistente)).thenReturn(response);

        service.upsertRespuestasByPaciente(1L, dto);

        verify(familiaRepository).save(familiaExistente);
        verify(events).publishEvent(any(MchatEvents.MchatFamiliaActualizadaEvent.class));
    }

    // ─── calcularScore ────────────────────────────────────────────────────────

    @Test
    void deberia_calcularScore_cuandoTodasRespuestasCorrectas() {
        var dto = new MchatSubmitDTO(
                true,  false, true,  true,  false,
                true,  true,  true,  true,  true,
                true,  false, true,  true,  true,
                true,  true,  true,  true,  true
        );

        assertThat(service.calcularScore(dto)).isEqualTo(0);
    }

    @Test
    void deberia_calcularScore_cuandoTodasRespuestasIncorrectas() {
        var dto = new MchatSubmitDTO(
                false, true,  false, false, true,
                false, false, false, false, false,
                false, true,  false, false, false,
                false, false, false, false, false
        );

        assertThat(service.calcularScore(dto)).isEqualTo(20);
    }

    @Test
    void deberia_calcularScore_cuandoPreguntasInvertidas_suman() {
        var dto = new MchatSubmitDTO(
                true,  true,  true,  true,  true,
                true,  true,  true,  true,  true,
                true,  true,  true,  true,  true,
                true,  true,  true,  true,  true
        );

        assertThat(service.calcularScore(dto)).isEqualTo(3);
    }

    @Test
    void deberia_calcularScore_unaSolaFalla() {
        var dto = new MchatSubmitDTO(
                false, false, true,  true,  false,
                true,  true,  true,  true,  true,
                true,  false, true,  true,  true,
                true,  true,  true,  true,  true
        );

        assertThat(service.calcularScore(dto)).isEqualTo(1);
    }

    // ─── calcularResultado ────────────────────────────────────────────────────

    @Test
    void deberia_calcularResultado_negativa_cuandoScoreBajoRiesgo() {
        assertThat(service.calcularResultado(0)).isEqualTo(MchatResultadoFinal.NEGATIVA);
        assertThat(service.calcularResultado(2)).isEqualTo(MchatResultadoFinal.NEGATIVA);
    }

    @Test
    void deberia_calcularResultado_nulo_cuandoScoreMedianoRiesgo() {
        assertThat(service.calcularResultado(3)).isNull();
        assertThat(service.calcularResultado(7)).isNull();
    }

    @Test
    void deberia_calcularResultado_positiva_cuandoScoreAltoRiesgo() {
        assertThat(service.calcularResultado(8)).isEqualTo(MchatResultadoFinal.POSITIVA);
        assertThat(service.calcularResultado(20)).isEqualTo(MchatResultadoFinal.POSITIVA);
    }

    // ─── aplicarSeguimiento ───────────────────────────────────────────────────

    @Test
    void deberia_aplicarSeguimiento_cuandoSegNuevo_creaYAsignaFallas() {
        var paciente = new Paciente();
        var familia = new MchatFamilia();
        familia.setScoreTotal(5);
        paciente.setMchatFamilia(familia);

        var dto = todosCorrectosSeguimiento();

        service.aplicarSeguimiento(paciente, dto);

        assertThat(paciente.getMchatSeguimiento()).isNotNull();
        assertThat(paciente.getMchatSeguimiento().getFallas()).isEqualTo(0);
        assertThat(familia.getResultadoFinal()).isEqualTo(MchatResultadoFinal.NEGATIVA);
    }

    @Test
    void deberia_aplicarSeguimiento_cuandoDosFallas_resultadoPositivo() {
        var paciente = new Paciente();
        var familia = new MchatFamilia();
        paciente.setMchatFamilia(familia);

        // 2 fallas: item1=false (no inverted → falla), item3=false (no inverted → falla), resto correctos
        var dto = new MchatSeguimientoDTO(
                false, false, false, true,  false,
                true,  true,  true,  true,  true,
                true,  false, true,  true,  true,
                true,  true,  true,  true,  true
        );

        service.aplicarSeguimiento(paciente, dto);

        assertThat(paciente.getMchatSeguimiento().getFallas()).isEqualTo(2);
        assertThat(familia.getResultadoFinal()).isEqualTo(MchatResultadoFinal.POSITIVA);
    }

    @Test
    void deberia_aplicarSeguimiento_cuandoSegExistente_loActualiza() {
        var paciente = new Paciente();
        var familia = new MchatFamilia();
        paciente.setMchatFamilia(familia);

        var segExistente = new MchatSeguimiento();
        segExistente.setItem1(false);
        segExistente.setFallas(5);
        paciente.setMchatSeguimiento(segExistente);

        service.aplicarSeguimiento(paciente, todosCorrectosSeguimiento());

        assertThat(paciente.getMchatSeguimiento()).isSameAs(segExistente);
        assertThat(paciente.getMchatSeguimiento().isItem1()).isTrue();
        assertThat(paciente.getMchatSeguimiento().getFallas()).isEqualTo(0);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private MchatSeguimientoDTO todosCorrectosSeguimiento() {
        // invertidos: 2, 5, 12 → valor false = correcto para ítems invertidos en seguimiento
        return new MchatSeguimientoDTO(
                true,  false, true,  true,  false,
                true,  true,  true,  true,  true,
                true,  false, true,  true,  true,
                true,  true,  true,  true,  true
        );
    }

    private MchatSubmitDTO todosCorrectos() {
        return new MchatSubmitDTO(
                true,  false, true,  true,  false,
                true,  true,  true,  true,  true,
                true,  false, true,  true,  true,
                true,  true,  true,  true,  true
        );
    }
}
