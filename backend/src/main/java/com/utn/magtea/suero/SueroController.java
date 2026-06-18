package com.utn.magtea.suero;

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

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/sueros")
@Tag(name = "Sueros")
@RequiredArgsConstructor
public class SueroController {

    private final SueroService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar sueros activos (paginado, filtro por rango y uso)")
    public PageResponse<SueroListDTO> findAll(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @RequestParam(required = false)          List<Integer> rangos,
            @RequestParam(required = false)          SueroUso uso,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {
        return service.findAll(page, size, rangos, uso, sortBy, sortDir);
    }

    @GetMapping("/disponibilidad-pool")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Disponibilidad de sueros por rango para armar pools")
    public List<SueroDisponibilidadDTO> getDisponibilidadPool() {
        return service.getDisponibilidadPool();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener suero por id")
    public SueroResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar suero")
    public ResponseEntity<SueroResponseDTO> create(@RequestBody @Valid SueroCreateDTO dto) {
        SueroResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar suero")
    public SueroResponseDTO update(@PathVariable Long id,
                                   @RequestBody @Valid SueroCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja suero (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
