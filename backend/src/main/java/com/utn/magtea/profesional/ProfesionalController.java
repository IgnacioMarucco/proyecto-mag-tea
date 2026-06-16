package com.utn.magtea.profesional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;

@RestController
@RequestMapping(ApiConstants.V1 + "/profesionales")
@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
@Tag(name = "Profesionales")
@RequiredArgsConstructor
public class ProfesionalController {

    private final ProfesionalService service;

    @GetMapping
    @Operation(summary = "Listar profesionales activos con paginación")
    public PageResponse<ProfesionalResponseDTO> findAll(
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "50")      int size,
            @RequestParam(required = false)         String q,
            @RequestParam(required = false)         Role rol,
            @RequestParam(defaultValue = "apellido") String sortBy,
            @RequestParam(defaultValue = "asc")     String sortDir) {
        return service.findAll(page, size, q, rol, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener profesional por id")
    public ProfesionalResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar profesional")
    public ResponseEntity<ProfesionalResponseDTO> create(@RequestBody @Valid ProfesionalCreateDTO dto) {
        ProfesionalResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar profesional")
    public ProfesionalResponseDTO update(@PathVariable Long id, @RequestBody @Valid ProfesionalUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dar de baja profesional (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
