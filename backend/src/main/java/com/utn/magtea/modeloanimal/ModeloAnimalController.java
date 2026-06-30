package com.utn.magtea.modeloanimal;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.suero.SueroUso;
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
@RequestMapping(ApiConstants.V1 + "/modelos-animales")
@Tag(name = "Modelos Animales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class ModeloAnimalController {

    private final ModeloAnimalService service;

    @GetMapping    @Operation(summary = "Listar modelos animales activos (filtro por pool, sexo, tipo, rango, estado y búsqueda por camada)")
    public PageResponse<ModeloAnimalListDTO> findAll(
            @RequestParam(defaultValue = "0")             int page,
            @RequestParam(defaultValue = "20")            int size,
            @RequestParam(required = false)               String q,
            @RequestParam(required = false)               Long poolId,
            @RequestParam(required = false)               List<SexoRaton> sexos,
            @RequestParam(required = false)               List<SueroUso> usos,
            @RequestParam(required = false)               List<Integer> rangos,
            @RequestParam(required = false)               List<EstadoProtocolo> estados,
            @RequestParam(required = false)               Boolean soloAlertas,
            @RequestParam(defaultValue = "fechaNacimiento") String sortBy,
            @RequestParam(defaultValue = "desc")          String sortDir) {
        return service.findAll(page, size, q, poolId, sexos, usos, rangos, estados, soloAlertas, sortBy, sortDir);
    }

    @GetMapping("/by-code/{identificador}")    @Operation(summary = "Obtener modelo animal por identificador alfanumérico")
    public ModeloAnimalResponseDTO findByIdentificador(@PathVariable String identificador) {
        return service.findByIdentificador(identificador);
    }

    @GetMapping("/by-code/{identificador}/reporte")    @Operation(summary = "Obtener datos completos del modelo animal para reporte PDF (proveniencia)")
    public ModeloAnimalReporteDTO getReporte(@PathVariable String identificador) {
        return service.getReporte(identificador);
    }

    @GetMapping("/{id}")    @Operation(summary = "Obtener modelo animal por id con alertas calculadas")
    public ModeloAnimalResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping    @Operation(summary = "Registrar modelo animal")
    public ResponseEntity<ModeloAnimalResponseDTO> create(@RequestBody @Valid ModeloAnimalCreateDTO dto) {
        ModeloAnimalResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")    @Operation(summary = "Actualizar datos principales del modelo animal")
    public ModeloAnimalResponseDTO update(@PathVariable Long id,
                                          @RequestBody @Valid ModeloAnimalCreateDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/inoculacion")    @Operation(summary = "Registrar fecha día 1 de inoculación y aportes de tubos")
    public ModeloAnimalResponseDTO registrarInoculacion(@PathVariable Long id,
                                                         @RequestBody @Valid ModeloAnimalInoculacionDTO dto) {
        return service.registrarInoculacion(id, dto);
    }

    @PatchMapping("/{id}/vocalizaciones")    @Operation(summary = "Registrar vocalizaciones ultrasónicas (VUS)")
    public ModeloAnimalResponseDTO registrarVocalizaciones(@PathVariable Long id,
                                                           @RequestBody @Valid VocalizacionesDTO dto) {
        return service.registrarVocalizaciones(id, dto);
    }

    @PatchMapping("/{id}/tres-camaras")    @Operation(summary = "Registrar estudio tres cámaras")
    public ModeloAnimalResponseDTO registrarTresCamaras(@PathVariable Long id,
                                                         @RequestBody @Valid TresCamarasDTO dto) {
        return service.registrarTresCamaras(id, dto);
    }

    @PatchMapping("/{id}/microscopia")    @Operation(summary = "Registrar datos de microscopía")
    public ModeloAnimalResponseDTO registrarMicroscopia(@PathVariable Long id,
                                                         @RequestBody @Valid ModeloAnimalMicroscopiaDTO dto) {
        return service.registrarMicroscopia(id, dto);
    }

    @PostMapping("/{id}/imagenes-microscopia")
    @Operation(summary = "Agregar imagen de microscopía al modelo animal")
    public ImagenMicroscopiaDTO agregarImagen(@PathVariable Long id,
                                              @RequestBody @Valid ImagenMicroscopiaCreateDTO dto) {
        return service.agregarImagen(id, dto);
    }

    @DeleteMapping("/{id}/imagenes-microscopia/{imagenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar imagen de microscopía del modelo animal")
    public void eliminarImagen(@PathVariable Long id, @PathVariable Long imagenId) {
        service.eliminarImagen(id, imagenId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)    @Operation(summary = "Dar de baja modelo animal (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
