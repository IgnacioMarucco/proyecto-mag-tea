package com.utn.magtea.formulariointeres;

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

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/formularios-interes")
@Tag(name = "Formularios de Interés")
@RequiredArgsConstructor
public class FormularioInteresController {

    private final FormularioInteresService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar formularios de interés con paginación")
    public PageResponse<FormularioInteresResponseDTO> findAll(
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "20")           int size,
            @RequestParam(required = false)              String q,
            @RequestParam(required = false)              List<EstadoFormulario> estados,
            @RequestParam(defaultValue = "fechaContacto") String sortBy,
            @RequestParam(defaultValue = "desc")          String sortDir) {
        return service.findAll(page, size, q, estados, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener formulario de interés por id")
    public FormularioInteresResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar formulario de interés (acceso público)")
    public ResponseEntity<FormularioInteresResponseDTO> create(@RequestBody @Valid FormularioInteresCreateDTO dto) {
        FormularioInteresResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar formulario de interés")
    public FormularioInteresResponseDTO update(@PathVariable Long id,
                                               @RequestBody @Valid FormularioInteresCreateDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Cambiar estado del formulario (PENDIENTE→CONTACTADO, CONTACTADO/PENDIENTE→DESCARTADO)")
    public FormularioInteresResponseDTO cambiarEstado(@PathVariable Long id,
                                                      @RequestBody @Valid EstadoUpdateDTO dto) {
        return service.cambiarEstado(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SECRETARIA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja formulario de interés (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
