package com.utn.magtea.tubo;

import com.utn.magtea.common.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/tubos")
@Tag(name = "Tubos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class TuboController {

    private final TuboService service;

    @PostMapping("/{id}/vaciar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Vaciar un tubo individual (registra motivo y libera la celda de la grilla)")
    public void vaciar(@PathVariable Long id,
                       @RequestBody @Valid VaciarTuboRequest req) {
        service.vaciar(id, req);
    }
}
