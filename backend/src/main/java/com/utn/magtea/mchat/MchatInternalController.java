package com.utn.magtea.mchat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mchat")
@Tag(name = "M-CHAT (interno)")
@RequiredArgsConstructor
public class MchatInternalController {

    private final MchatService service;

    @GetMapping("/paciente/{pacienteId}/respuestas")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL','CUERPO_MEDICO','ROTANTE_CLINICA','CUERPO_TECNICO')")
    @Operation(summary = "Obtener respuestas M-CHAT del paciente")
    public MchatRespuestasResponseDTO getRespuestas(@PathVariable Long pacienteId) {
        return service.getRespuestasByPaciente(pacienteId);
    }

    @PutMapping("/paciente/{pacienteId}/respuestas")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL','CUERPO_MEDICO','ROTANTE_CLINICA')")
    @Operation(summary = "Registrar o reemplazar respuestas M-CHAT del paciente")
    public MchatRespuestasResponseDTO upsertRespuestas(@PathVariable Long pacienteId,
                                                       @RequestBody @Valid MchatSubmitDTO dto) {
        return service.upsertRespuestasByPaciente(pacienteId, dto);
    }
}
