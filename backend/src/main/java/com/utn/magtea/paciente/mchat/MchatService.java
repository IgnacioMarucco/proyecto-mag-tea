package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.TipoPaciente;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MchatService {

    private final ApplicationEventPublisher events;
    private final MchatTokenService mchatTokenService;
    private final MchatFamiliaRepository familiaRepository;
    private final MchatFamiliaMapper mapper;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public MchatPublicResponseDTO validarToken(String token) {
        MchatInfoDTO info = mchatTokenService.validarToken(token);
        boolean yaCompletado = familiaRepository.existsByPaciente_Id(info.id());
        return new MchatPublicResponseDTO(info.nombreNino(), info.apellidoNino(), yaCompletado);
    }

    @Transactional
    public MchatFamiliaResponseDTO guardarRespuestas(String token, MchatSubmitDTO dto) {
        MchatInfoDTO info = mchatTokenService.validarToken(token);

        if (familiaRepository.existsByPaciente_Id(info.id())) {
            throw new BusinessRuleException("El formulario M-CHAT ya fue completado para este paciente");
        }

        int score = calcularScore(dto);

        MchatFamilia familia = new MchatFamilia();
        familia.setPaciente(em.getReference(Paciente.class, info.id()));
        mapearRespuestas(familia, dto);
        familia.setScoreTotal(score);
        familia.setResultadoFinal(calcularResultado(score));
        familiaRepository.save(familia);

        events.publishEvent(new MchatEvents.MchatFamiliaGuardadaEvent(info.id()));
        return mapper.toDTO(familia);
    }

    @Transactional(readOnly = true)
    public MchatFamiliaResponseDTO getRespuestasByPaciente(Long pacienteId) {
        MchatFamilia familia = familiaRepository.findByPaciente_Id(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay respuestas M-CHAT registradas para el paciente con id " + pacienteId));
        return mapper.toDTO(familia);
    }

    @Transactional
    public MchatFamiliaResponseDTO upsertRespuestasByPaciente(Long pacienteId, MchatSubmitDTO dto) {
        mchatTokenService.verificarActivo(pacienteId);

        Paciente paciente = em.find(Paciente.class, pacienteId);
        if (paciente == null || !paciente.isActivo())
            throw new ResourceNotFoundException("Paciente con id " + pacienteId + " no existe");
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL)
            throw new BusinessRuleException("El formulario M-CHAT no aplica para pacientes caso control");

        MchatFamilia familia = familiaRepository.findByPaciente_Id(pacienteId)
                .orElseGet(() -> {
                    MchatFamilia nueva = new MchatFamilia();
                    nueva.setPaciente(em.getReference(Paciente.class, pacienteId));
                    return nueva;
                });

        mapearRespuestas(familia, dto);
        int score = calcularScore(dto);
        familia.setScoreTotal(score);
        familia.setResultadoFinal(calcularResultado(score));
        familiaRepository.save(familia);

        events.publishEvent(new MchatEvents.MchatFamiliaActualizadaEvent(pacienteId));
        return mapper.toDTO(familia);
    }

    /**
     * Actualiza el seguimiento M-CHAT. Recibe la entidad ya cargada por PacienteService;
     * no guarda — el llamador persiste via cascade.
     */
    @Transactional
    public void aplicarSeguimiento(Paciente paciente, MchatSeguimientoDTO dto) {
        MchatSeguimiento seg = Optional.ofNullable(paciente.getMchatSeguimiento())
                .orElseGet(() -> { var s = new MchatSeguimiento(); s.setPaciente(paciente); return s; });
        seg.setItem1(dto.item1());   seg.setItem2(dto.item2());   seg.setItem3(dto.item3());
        seg.setItem4(dto.item4());   seg.setItem5(dto.item5());   seg.setItem6(dto.item6());
        seg.setItem7(dto.item7());   seg.setItem8(dto.item8());   seg.setItem9(dto.item9());
        seg.setItem10(dto.item10()); seg.setItem11(dto.item11()); seg.setItem12(dto.item12());
        seg.setItem13(dto.item13()); seg.setItem14(dto.item14()); seg.setItem15(dto.item15());
        seg.setItem16(dto.item16()); seg.setItem17(dto.item17()); seg.setItem18(dto.item18());
        seg.setItem19(dto.item19()); seg.setItem20(dto.item20());

        int fallas = MchatScoringUtil.calcularScore(dto.toBooleanArray());
        seg.setFallas(fallas);
        paciente.setMchatSeguimiento(seg);
        paciente.getMchatFamilia().setResultadoFinal(
                fallas <= 1 ? MchatResultadoFinal.NEGATIVA : MchatResultadoFinal.POSITIVA);
    }

    int calcularScore(MchatSubmitDTO d) {
        return MchatScoringUtil.calcularScore(d.toBooleanArray());
    }

    MchatResultadoFinal calcularResultado(int score) {
        if (score <= 2 || score >= 8) {
            return score <= 2 ? MchatResultadoFinal.NEGATIVA : MchatResultadoFinal.POSITIVA;
        }
        return null;
    }

    private void mapearRespuestas(MchatFamilia r, MchatSubmitDTO dto) {
        r.setP1(dto.p1());   r.setP2(dto.p2());   r.setP3(dto.p3());
        r.setP4(dto.p4());   r.setP5(dto.p5());   r.setP6(dto.p6());
        r.setP7(dto.p7());   r.setP8(dto.p8());   r.setP9(dto.p9());
        r.setP10(dto.p10()); r.setP11(dto.p11()); r.setP12(dto.p12());
        r.setP13(dto.p13()); r.setP14(dto.p14()); r.setP15(dto.p15());
        r.setP16(dto.p16()); r.setP17(dto.p17()); r.setP18(dto.p18());
        r.setP19(dto.p19()); r.setP20(dto.p20());
    }
}
