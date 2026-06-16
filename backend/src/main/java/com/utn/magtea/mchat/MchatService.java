package com.utn.magtea.mchat;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.PacienteService;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatFamiliaMapper;
import com.utn.magtea.paciente.mchat.MchatFamiliaRepository;
import com.utn.magtea.paciente.mchat.MchatFamiliaResponseDTO;
import com.utn.magtea.paciente.mchatseguimiento.MchatResultadoFinal;
import com.utn.magtea.paciente.mchatseguimiento.PacienteMchatInfoDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MchatService {

    private final PacienteService pacienteService;
    private final MchatFamiliaRepository familiaRepository;
    private final MchatFamiliaMapper mapper;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public MchatPublicResponseDTO validarToken(String token) {
        PacienteMchatInfoDTO info = pacienteService.validarTokenMchat(token);
        boolean yaCompletado = familiaRepository.existsByPaciente_Id(info.id());
        return new MchatPublicResponseDTO(info.nombreNino(), info.apellidoNino(), yaCompletado);
    }

    @Transactional
    public MchatFamiliaResponseDTO guardarRespuestas(String token, MchatSubmitDTO dto) {
        PacienteMchatInfoDTO info = pacienteService.validarTokenMchat(token);

        if (familiaRepository.existsByPaciente_Id(info.id())) {
            throw new BusinessRuleException("El formulario M-CHAT ya fue completado para este paciente");
        }

        int score = calcularScore(dto);

        MchatFamilia familia = new MchatFamilia();
        familia.setPaciente(em.getReference(com.utn.magtea.paciente.Paciente.class, info.id()));
        mapearRespuestas(familia, dto);
        familia.setScoreTotal(score);
        familia.setResultadoFinal(calcularResultado(score));
        familiaRepository.save(familia);

        pacienteService.procesarMchatPublico(info.id());
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
        pacienteService.findById(pacienteId);

        MchatFamilia familia = familiaRepository.findByPaciente_Id(pacienteId)
                .orElseGet(() -> {
                    MchatFamilia nueva = new MchatFamilia();
                    nueva.setPaciente(em.getReference(com.utn.magtea.paciente.Paciente.class, pacienteId));
                    return nueva;
                });

        mapearRespuestas(familia, dto);
        int score = calcularScore(dto);
        familia.setScoreTotal(score);
        familia.setResultadoFinal(calcularResultado(score));
        familiaRepository.save(familia);

        pacienteService.actualizarMchatInterno(pacienteId);
        return mapper.toDTO(familia);
    }

    int calcularScore(MchatSubmitDTO d) {
        return MchatScoringUtil.calcularScore(d.toBooleanArray());
    }

    MchatResultadoFinal calcularResultado(int score) {
        if (score <= 2 || score >= 8) {
            return score <= 2 ? MchatResultadoFinal.NEGATIVA : MchatResultadoFinal.POSITIVA;
        }
        return null; // mediano riesgo: requiere seguimiento para determinar resultado
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
