package com.utn.magtea.mchat;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.paciente.mchat.MchatFamiliaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/pacientes")
@Tag(name = "M-CHAT (interno)")
@RequiredArgsConstructor
public class MchatInternalController {

    private final MchatService service;

    @GetMapping("/{pacienteId}/mchat/respuestas")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL','CUERPO_MEDICO','ROTANTE_CLINICA','CUERPO_TECNICO')")
    @Operation(summary = "Obtener respuestas M-CHAT del paciente")
    public MchatFamiliaResponseDTO getRespuestas(@PathVariable Long pacienteId) {
        return service.getRespuestasByPaciente(pacienteId);
    }

    @PutMapping("/{pacienteId}/mchat/respuestas")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL','CUERPO_MEDICO','ROTANTE_CLINICA')")
    @Operation(summary = "Registrar o reemplazar respuestas M-CHAT del paciente")
    public MchatFamiliaResponseDTO upsertRespuestas(@PathVariable Long pacienteId,
                                                    @RequestBody @Valid MchatSubmitDTO dto) {
        return service.upsertRespuestasByPaciente(pacienteId, dto);
    }
}
