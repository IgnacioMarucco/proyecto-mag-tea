package com.utn.magtea.mchat;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.paciente.mchat.MchatFamiliaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/public/mchat")
@Tag(name = "M-CHAT (público)")
@RequiredArgsConstructor
public class MchatController {

    private final MchatService service;

    @GetMapping("/{token}")
    @Operation(summary = "Verificar token y obtener datos del formulario")
    public MchatPublicResponseDTO getFormulario(@PathVariable String token) {
        return service.validarToken(token);
    }

    @PostMapping("/{token}")
    @Operation(summary = "Enviar respuestas del M-CHAT")
    public MchatFamiliaResponseDTO submitRespuestas(@PathVariable String token,
                                                    @RequestBody MchatSubmitDTO dto) {
        return service.guardarRespuestas(token, dto);
    }
}
