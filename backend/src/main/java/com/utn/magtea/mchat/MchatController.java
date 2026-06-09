package com.utn.magtea.mchat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/mchat")
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
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar respuestas del M-CHAT")
    public void submitRespuestas(@PathVariable String token,
                                 @RequestBody MchatSubmitDTO dto) {
        service.guardarRespuestas(token, dto);
    }
}
