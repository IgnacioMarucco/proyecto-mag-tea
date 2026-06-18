package com.utn.magtea.caja;

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
@RequestMapping(ApiConstants.V1 + "/cajas")
@Tag(name = "Cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar cajas activas (paginado, filtro por freezer)")
    public PageResponse<CajaListDTO> findAll(
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "20")      int size,
            @RequestParam(required = false)         String freezer,
            @RequestParam(defaultValue = "freezer") String sortBy,
            @RequestParam(defaultValue = "asc")     String sortDir) {
        return service.findAll(page, size, freezer, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener caja por id")
    public CajaResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Crear caja")
    public ResponseEntity<CajaResponseDTO> create(@RequestBody @Valid CajaCreateDTO dto) {
        CajaResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar caja")
    public CajaResponseDTO update(@PathVariable Long id,
                                  @RequestBody @Valid CajaCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja caja (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/ocupacion")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Consultar posiciones ocupadas en la caja")
    public CajaOcupacionDTO getOcupacion(@PathVariable Long id) {
        return service.getOcupacion(id);
    }
}
