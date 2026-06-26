package com.utn.magtea.paciente;

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
import com.utn.magtea.paciente.cars.CarsDTO;
import com.utn.magtea.paciente.criterios.CriteriosDTO;
import com.utn.magtea.paciente.mchat.MchatSeguimientoDTO;
import com.utn.magtea.paciente.vineland.VinelandDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/pacientes")
@Tag(name = "Pacientes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_MEDICO', 'INVESTIGADOR_PRINCIPAL')")
public class PacienteController {

    private final PacienteService service;

    @GetMapping    @Operation(summary = "Listar pacientes activos con paginación")
    public PageResponse<PacienteListDTO> findAll(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(required = false)            String q,
            @RequestParam(required = false)            List<PacienteEstado> estados,
            @RequestParam(required = false)            List<TipoPaciente> tipos,
            @RequestParam(defaultValue = "proximaFechaEvento")  String sortBy,
            @RequestParam(defaultValue = "asc")                  String sortDir,
            @RequestParam(required = false)            LocalDate fechaEvento,
            @RequestParam(required = false)            String categoriaAgenda) {
        return service.findAll(page, size, q, estados, tipos, sortBy, sortDir, fechaEvento, categoriaAgenda);
    }

    @GetMapping("/{codigo}")    @Operation(summary = "Obtener paciente por código alfanumérico")
    public PacienteResponseDTO findByCodigo(@PathVariable String codigo) {
        return service.findByCodigoFull(codigo);
    }

    @GetMapping("/by-codigo/{codigo}")
    @PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'CUERPO_MEDICO', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Buscar paciente por código numérico (para alta de suero)")
    public PacientePorCodigoDTO findByCodigoSimple(@PathVariable String codigo) {
        return service.findByCodigoNumerico(codigo);
    }

    @PostMapping    @Operation(summary = "Registrar paciente")
    public ResponseEntity<PacienteResponseDTO> create(@RequestBody @Valid PacienteCreateDTO dto) {
        PacienteResponseDTO created = service.create(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}").buildAndExpand(created.codigoNumerico()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{codigo}")    @Operation(summary = "Actualizar datos básicos del paciente")
    public PacienteResponseDTO update(@PathVariable String codigo,
                                      @RequestBody @Valid PacienteUpdateDTO dto) {
        return service.update(codigo, dto);
    }

    @PatchMapping("/{codigo}/primera-visita")    @Operation(summary = "Actualizar fecha y hora de la primera visita")
    public PacienteResponseDTO updatePrimeraVisita(@PathVariable String codigo,
                                                   @RequestBody @Valid PacientePrimeraVisitaDTO dto) {
        return service.updatePrimeraVisita(codigo, dto);
    }

    @PatchMapping("/{codigo}/consentimiento")    @Operation(summary = "Registrar consentimiento informado")
    public PacienteResponseDTO updateConsentimiento(@PathVariable String codigo,
                                                    @RequestBody @Valid PacienteConsentimientoDTO dto) {
        return service.updateConsentimiento(codigo, dto);
    }

    @PatchMapping("/{codigo}/criterios")    @Operation(summary = "Registrar criterios de inclusión/exclusión")
    public PacienteResponseDTO updateCriterios(@PathVariable String codigo,
                                               @RequestBody @Valid CriteriosDTO dto) {
        return service.updateCriterios(codigo, dto);
    }

    @PatchMapping("/{codigo}/mchat-seguimiento")    @Operation(summary = "Registrar resultado del seguimiento M-CHAT-R/F (solo si score 3-7)")
    public PacienteResponseDTO updateMchatSeguimiento(@PathVariable String codigo,
                                                      @RequestBody @Valid MchatSeguimientoDTO dto) {
        return service.updateMchatSeguimiento(codigo, dto);
    }

    @PatchMapping("/{codigo}/cars")    @Operation(summary = "Registrar puntuaciones CARS-2")
    public PacienteResponseDTO updateCars(@PathVariable String codigo,
                                          @RequestBody @Valid CarsDTO dto) {
        return service.updateCars(codigo, dto);
    }

    @PatchMapping("/{codigo}/vineland")    @Operation(summary = "Registrar puntuaciones Vineland")
    public PacienteResponseDTO updateVineland(@PathVariable String codigo,
                                              @RequestBody @Valid VinelandDTO dto) {
        return service.updateVineland(codigo, dto);
    }

    @PatchMapping("/{codigo}/segunda-visita")    @Operation(summary = "Registrar fecha de extracción de sangre (segunda visita)")
    public PacienteResponseDTO updateSegundaVisita(@PathVariable String codigo,
                                                   @RequestBody @Valid PacienteSegundaVisitaDTO dto) {
        return service.updateSegundaVisita(codigo, dto);
    }

    @PostMapping("/{codigo}/reenviar-mchat")    @Operation(summary = "Regenerar y reenviar el enlace M-CHAT por mail")
    public PacienteResponseDTO reenviarMchat(@PathVariable String codigo) {
        return service.reenviarMchat(codigo);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja paciente (baja lógica)")
    public void delete(@PathVariable String codigo) {
        service.delete(codigo);
    }
}
