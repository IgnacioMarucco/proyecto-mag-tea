package com.utn.magtea.pool;

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

import com.utn.magtea.suero.SueroUso;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/pools")
@Tag(name = "Pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar pools activos (paginado, filtro por rango y uso)")
    public PageResponse<PoolListDTO> findAll(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @RequestParam(required = false)          List<Integer> rangos,
            @RequestParam(required = false)          List<SueroUso> usos,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {
        return service.findAll(page, size, rangos, usos, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener pool por id")
    public PoolResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Crear pool")
    public ResponseEntity<PoolResponseDTO> create(@RequestBody @Valid PoolCreateDTO dto) {
        PoolResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar pool (caja, tubos, fechaCreacion)")
    public PoolResponseDTO update(@PathVariable Long id,
                                  @RequestBody @Valid PoolUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja pool (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
