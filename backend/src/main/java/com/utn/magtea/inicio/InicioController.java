package com.utn.magtea.inicio;

import com.utn.magtea.common.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/inicio")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Inicio", description = "Datos de la pantalla de inicio según rol")
@RequiredArgsConstructor
public class InicioController {

    private final InicioService inicioService;

    @GetMapping
    @Operation(summary = "Agenda semanal, alertas y actividad reciente según el rol del usuario autenticado")
    public InicioResponseDTO getInicio(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        return inicioService.getInicio(role);
    }
}
