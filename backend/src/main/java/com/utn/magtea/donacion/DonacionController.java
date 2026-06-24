package com.utn.magtea.donacion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;

@RestController
@RequestMapping(ApiConstants.V1 + "/donaciones")
@Tag(name = "Donaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
public class DonacionController {

    private final DonacionService service;

    @GetMapping
    @Operation(summary = "Listar donaciones (requiere rol INVESTIGADOR_PRINCIPAL)")
    public PageResponse<DonacionResponseDTO> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.findAll(page, size);
    }
}
