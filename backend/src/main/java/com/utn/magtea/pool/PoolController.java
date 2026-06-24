package com.utn.magtea.pool;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.VaciarTuboRequest;
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
@RequestMapping(ApiConstants.V1 + "/pools")
@Tag(name = "Pools")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class PoolController {

    private final PoolService service;

    @GetMapping    @Operation(summary = "Listar pools activos (paginado, filtro por rango y uso)")
    public PageResponse<PoolListDTO> findAll(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(required = false)           String q,
            @RequestParam(required = false)           List<Integer> rangos,
            @RequestParam(required = false)           List<SueroUso> usos,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {
        return service.findAll(page, size, q, rangos, usos, sortBy, sortDir);
    }

    @GetMapping("/{id}")    @Operation(summary = "Obtener pool por id")
    public PoolResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/by-codigo/{codigo}")    @Operation(summary = "Obtener pool por código alfanumérico")
    public PoolResponseDTO findByCodigo(@PathVariable String codigo) {
        return service.findByCodigo(codigo);
    }

    @PostMapping    @Operation(summary = "Crear pool")
    public ResponseEntity<PoolResponseDTO> create(@RequestBody @Valid PoolCreateDTO dto) {
        PoolResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")    @Operation(summary = "Actualizar pool (caja, tubos, fechaCreacion)")
    public PoolResponseDTO update(@PathVariable Long id,
                                  @RequestBody @Valid PoolUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)    @Operation(summary = "Dar de baja pool (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/liberar-grilla")    @Operation(summary = "Vaciar todos los tubos del pool y liberar sus celdas de la grilla")
    public PoolResponseDTO liberarGrilla(@PathVariable Long id,
                                         @RequestBody @Valid VaciarTuboRequest req) {
        return service.liberarGrilla(id, req);
    }
}
