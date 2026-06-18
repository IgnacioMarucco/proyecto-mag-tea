package com.utn.magtea.modeloanimal;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
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
@RequestMapping(ApiConstants.V1 + "/modelos-animales")
@Tag(name = "Modelos Animales")
@RequiredArgsConstructor
public class ModeloAnimalController {

    private final ModeloAnimalService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar modelos animales activos (filtro por pool y sexo)")
    public PageResponse<ModeloAnimalListDTO> findAll(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size,
            @RequestParam(required = false)          Long poolId,
            @RequestParam(required = false)          SexoRaton sexo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")     String sortDir) {
        return service.findAll(page, size, poolId, sexo, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener modelo animal por id con alertas calculadas")
    public ModeloAnimalResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar modelo animal")
    public ResponseEntity<ModeloAnimalResponseDTO> create(@RequestBody @Valid ModeloAnimalCreateDTO dto) {
        ModeloAnimalResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar datos principales del modelo animal")
    public ModeloAnimalResponseDTO update(@PathVariable Long id,
                                          @RequestBody @Valid ModeloAnimalCreateDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/vocalizaciones")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar vocalizaciones ultrasónicas (VUS)")
    public ModeloAnimalResponseDTO registrarVocalizaciones(@PathVariable Long id,
                                                           @RequestBody @Valid VocalizacionesDTO dto) {
        return service.registrarVocalizaciones(id, dto);
    }

    @PatchMapping("/{id}/tres-camaras")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar estudio tres cámaras")
    public ModeloAnimalResponseDTO registrarTresCamaras(@PathVariable Long id,
                                                         @RequestBody @Valid TresCamarasDTO dto) {
        return service.registrarTresCamaras(id, dto);
    }

    @PatchMapping("/{id}/microscopia")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar datos de microscopía")
    public ModeloAnimalResponseDTO registrarMicroscopia(@PathVariable Long id,
                                                         @RequestBody @Valid ModeloAnimalMicroscopiaDTO dto) {
        return service.registrarMicroscopia(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja modelo animal (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
