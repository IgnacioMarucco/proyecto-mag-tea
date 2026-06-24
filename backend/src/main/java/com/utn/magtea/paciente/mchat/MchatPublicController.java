package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/public/mchat")
@Tag(name = "M-CHAT (público)")
@RequiredArgsConstructor
public class MchatPublicController {

    private final MchatService service;

    @GetMapping("/{token}")
    @Operation(summary = "Verificar token y obtener datos del formulario")
    public MchatPublicResponseDTO getFormulario(@PathVariable String token) {
        return service.validarToken(token);
    }

    @PostMapping("/{token}")
    @Operation(summary = "Enviar respuestas del M-CHAT")
    public MchatFamiliaResponseDTO submitRespuestas(@PathVariable String token,
                                                    @RequestBody @Valid MchatSubmitDTO dto) {
        return service.guardarRespuestas(token, dto);
    }
}
