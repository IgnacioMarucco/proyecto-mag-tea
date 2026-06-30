package com.utn.magtea.suero;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;
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
@RequestMapping(ApiConstants.V1 + "/sueros")
@Tag(name = "Sueros")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class SueroController {

    private final SueroService service;

    @GetMapping    @Operation(summary = "Listar sueros activos (paginado, filtro por rango, uso y paciente)")
    public PageResponse<SueroListDTO> findAll(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @RequestParam(required = false)          String q,
            @RequestParam(required = false)          List<Integer> rangos,
            @RequestParam(required = false)          List<SueroUso> usos,
            @RequestParam(required = false)           String codigoPaciente,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {
        return service.findAll(page, size, q, rangos, usos, codigoPaciente, sortBy, sortDir);
    }

    @GetMapping("/disponibilidad-pool")    @Operation(summary = "Disponibilidad de sueros por rango para armar pools")
    public List<SueroDisponibilidadDTO> getDisponibilidadPool() {
        return service.getDisponibilidadPool();
    }

    @GetMapping("/by-codigo/{codigoNumerico}")    @Operation(summary = "Obtener suero por código del paciente")
    public SueroResponseDTO findByCodigoNumerico(@PathVariable String codigoNumerico) {
        return service.findByCodigoNumerico(codigoNumerico);
    }

    @GetMapping("/{id}")    @Operation(summary = "Obtener suero por id")
    public SueroResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping    @Operation(summary = "Registrar suero")
    public ResponseEntity<SueroResponseDTO> create(@RequestBody @Valid SueroCreateDTO dto) {
        SueroResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")    @Operation(summary = "Actualizar suero")
    public SueroResponseDTO update(@PathVariable Long id,
                                   @RequestBody @Valid SueroUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)    @Operation(summary = "Dar de baja suero (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/liberar-grilla")    @Operation(summary = "Vaciar todos los tubos del suero y liberar sus celdas de la grilla")
    public SueroResponseDTO liberarGrilla(@PathVariable Long id,
                                          @RequestBody @Valid VaciarTuboRequest req) {
        return service.liberarGrilla(id, req);
    }
}
