package com.utn.magtea.profesional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.utn.magtea.common.ApiConstants;

@RestController
@RequestMapping(ApiConstants.V1 + "/perfil")
@Tag(name = "Mi Perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final ProfesionalService service;

    @GetMapping
    @Operation(summary = "Obtener mis datos de perfil")
    public ProfesionalResponseDTO me(Authentication authentication) {
        return service.findByEmail(authentication.getName());
    }

    @PutMapping
    @Operation(summary = "Actualizar mis datos de perfil")
    public ProfesionalResponseDTO updateMe(Authentication authentication, @RequestBody @Valid PerfilUpdateDTO dto) {
        return service.updateSelf(authentication.getName(), dto);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambiar mi contraseña")
    public void changePassword(Authentication authentication, @RequestBody @Valid CambiarPasswordDTO dto) {
        service.changePassword(authentication.getName(), dto);
    }
}
