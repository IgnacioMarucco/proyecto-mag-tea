package com.utn.magtea.camada;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(ApiConstants.V1 + "/camadas")
@Tag(name = "Camadas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class CamadaController {

    private final CamadaService service;

    @GetMapping    @Operation(summary = "Listar camadas activas (paginado)")
    public PageResponse<CamadaListDTO> findAll(
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "20")    int size,
            @RequestParam(required = false)       String q,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc")   String sortDir) {
        return service.findAll(page, size, q, sortBy, sortDir);
    }

    @GetMapping("/{id}")    @Operation(summary = "Obtener camada por id")
    public CamadaResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping    @Operation(summary = "Crear camada")
    public ResponseEntity<CamadaResponseDTO> create(@RequestBody @Valid CamadaCreateDTO dto) {
        CamadaResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")    @Operation(summary = "Actualizar camada")
    public CamadaResponseDTO update(@PathVariable Long id,
                                    @RequestBody @Valid CamadaCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)    @Operation(summary = "Dar de baja camada (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
