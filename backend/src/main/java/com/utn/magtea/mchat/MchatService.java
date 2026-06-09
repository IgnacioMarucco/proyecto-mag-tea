package com.utn.magtea.mchat;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.MchatResultadoFinal;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MchatService {

    private final PacienteRepository pacienteRepository;
    private final MchatRespuestasRepository respuestasRepository;
    private final MchatRespuestasMapper mapper;

    @Transactional(readOnly = true)
    public MchatPublicResponseDTO validarToken(String token) {
        Paciente paciente = buscarPorToken(token);
        boolean yaCompletado = respuestasRepository.existsByPacienteId(paciente.getId());
        return new MchatPublicResponseDTO(paciente.getNombreNino(), paciente.getApellidoNino(), yaCompletado);
    }

    @Transactional
    public void guardarRespuestas(String token, MchatSubmitDTO dto) {
        Paciente paciente = buscarPorToken(token);

        if (respuestasRepository.existsByPacienteId(paciente.getId())) {
            throw new BusinessRuleException("El formulario M-CHAT ya fue completado para este paciente");
        }

        int score = calcularScore(dto);

        MchatRespuestas respuestas = new MchatRespuestas();
        mapearRespuestas(respuestas, dto);
        respuestas.setPacienteId(paciente.getId());
        respuestas.setScoreTotal(score);
        respuestasRepository.save(respuestas);

        actualizarPacienteConScore(paciente, score);
        paciente.refreshEstadoClinico();

        // Invalidar token
        paciente.setMchatToken(null);
        paciente.setMchatTokenExpiry(null);
        pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true)
    public MchatRespuestasResponseDTO getRespuestasByPaciente(Long pacienteId) {
        MchatRespuestas r = respuestasRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay respuestas M-CHAT registradas para el paciente con id " + pacienteId));
        return mapper.toDTO(r);
    }

    @Transactional
    public MchatRespuestasResponseDTO upsertRespuestasByPaciente(Long pacienteId, MchatSubmitDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con id " + pacienteId + " no existe"));

        MchatRespuestas respuestas = respuestasRepository.findByPacienteId(pacienteId)
                .orElseGet(MchatRespuestas::new);

        respuestas.setPacienteId(pacienteId);
        mapearRespuestas(respuestas, dto);
        int score = calcularScore(dto);
        respuestas.setScoreTotal(score);
        respuestasRepository.save(respuestas);

        actualizarPacienteConScore(paciente, score);

        if (score < 3 || score > 7) {
            paciente.setMchatSeguimientoFallas(null);
            limpiarSeguimientoItems(paciente);
        }

        paciente.refreshEstadoClinico();
        pacienteRepository.save(paciente);
        return mapper.toDTO(respuestas);
    }

    // Preguntas invertidas: 2, 5, 12 → Sí = falla
    int calcularScore(MchatSubmitDTO d) {
        int fallas = 0;
        if (!d.p1())  fallas++;
        if (d.p2())   fallas++;
        if (!d.p3())  fallas++;
        if (!d.p4())  fallas++;
        if (d.p5())   fallas++;
        if (!d.p6())  fallas++;
        if (!d.p7())  fallas++;
        if (!d.p8())  fallas++;
        if (!d.p9())  fallas++;
        if (!d.p10()) fallas++;
        if (!d.p11()) fallas++;
        if (d.p12())  fallas++;
        if (!d.p13()) fallas++;
        if (!d.p14()) fallas++;
        if (!d.p15()) fallas++;
        if (!d.p16()) fallas++;
        if (!d.p17()) fallas++;
        if (!d.p18()) fallas++;
        if (!d.p19()) fallas++;
        if (!d.p20()) fallas++;
        return fallas;
    }

    private void actualizarPacienteConScore(Paciente paciente, int score) {
        paciente.setMchatScoreTotal(score);
        if (score <= 2) {
            paciente.setMchatResultadoFinal(MchatResultadoFinal.NEGATIVA);
        } else if (score >= 8) {
            paciente.setMchatResultadoFinal(MchatResultadoFinal.POSITIVA);
        } else {
            // Rango 3-7: pendiente de seguimiento, limpiar resultado anterior si existía
            paciente.setMchatResultadoFinal(null);
        }
    }

    private void mapearRespuestas(MchatRespuestas r, MchatSubmitDTO dto) {
        r.setP1(dto.p1());   r.setP2(dto.p2());   r.setP3(dto.p3());
        r.setP4(dto.p4());   r.setP5(dto.p5());   r.setP6(dto.p6());
        r.setP7(dto.p7());   r.setP8(dto.p8());   r.setP9(dto.p9());
        r.setP10(dto.p10()); r.setP11(dto.p11()); r.setP12(dto.p12());
        r.setP13(dto.p13()); r.setP14(dto.p14()); r.setP15(dto.p15());
        r.setP16(dto.p16()); r.setP17(dto.p17()); r.setP18(dto.p18());
        r.setP19(dto.p19()); r.setP20(dto.p20());
    }

    private void limpiarSeguimientoItems(Paciente paciente) {
        paciente.setSeguimientoItem1(null);  paciente.setSeguimientoItem2(null);
        paciente.setSeguimientoItem3(null);  paciente.setSeguimientoItem4(null);
        paciente.setSeguimientoItem5(null);  paciente.setSeguimientoItem6(null);
        paciente.setSeguimientoItem7(null);  paciente.setSeguimientoItem8(null);
        paciente.setSeguimientoItem9(null);  paciente.setSeguimientoItem10(null);
        paciente.setSeguimientoItem11(null); paciente.setSeguimientoItem12(null);
        paciente.setSeguimientoItem13(null); paciente.setSeguimientoItem14(null);
        paciente.setSeguimientoItem15(null); paciente.setSeguimientoItem16(null);
        paciente.setSeguimientoItem17(null); paciente.setSeguimientoItem18(null);
        paciente.setSeguimientoItem19(null); paciente.setSeguimientoItem20(null);
    }

    private Paciente buscarPorToken(String token) {
        return pacienteRepository.findByMchatToken(token)
                .filter(p -> p.getMchatTokenExpiry() != null
                        && p.getMchatTokenExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El enlace no es válido o ha expirado"));
    }
}
