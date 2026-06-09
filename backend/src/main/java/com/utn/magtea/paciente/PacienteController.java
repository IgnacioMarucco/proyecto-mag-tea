package com.utn.magtea.paciente;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.utn.magtea.common.PageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Listar pacientes activos con paginación")
    public PageResponse<PacienteResponseDTO> findAll(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(required = false)            String q,
            @RequestParam(required = false)            List<PacienteEstado> estados,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir) {
        return service.findAll(page, size, q, estados, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Obtener paciente por id")
    public PacienteResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Registrar paciente")
    public PacienteResponseDTO create(@RequestBody @Valid PacienteCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Actualizar datos básicos del paciente")
    public PacienteResponseDTO update(@PathVariable Long id,
                                      @RequestBody @Valid PacienteUpdateDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/primera-visita")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Actualizar fecha y hora de la primera visita")
    public PacienteResponseDTO updatePrimeraVisita(@PathVariable Long id,
                                                   @RequestBody @Valid PacientePrimeraVisitaDTO dto) {
        return service.updatePrimeraVisita(id, dto);
    }

    @PatchMapping("/{id}/consentimiento")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Registrar consentimiento informado")
    public PacienteResponseDTO updateConsentimiento(@PathVariable Long id,
                                                    @RequestBody PacienteConsentimientoDTO dto) {
        return service.updateConsentimiento(id, dto);
    }

    @PatchMapping("/{id}/criterios")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Registrar criterios de inclusión/exclusión y consentimiento")
    public PacienteResponseDTO updateCriterios(@PathVariable Long id,
                                               @RequestBody PacienteCriteriosDTO dto) {
        return service.updateCriterios(id, dto);
    }

    @PatchMapping("/{id}/mchat-seguimiento")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Registrar resultado del seguimiento M-CHAT-R/F (solo si score 3-7)")
    public PacienteResponseDTO updateMchatSeguimiento(@PathVariable Long id,
                                                      @RequestBody PacienteMchatSeguimientoDTO dto) {
        return service.updateMchatSeguimiento(id, dto);
    }

    @PatchMapping("/{id}/cars")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Registrar puntuaciones CARS-2")
    public PacienteResponseDTO updateCars(@PathVariable Long id,
                                          @RequestBody @Valid PacienteCarsDTO dto) {
        return service.updateCars(id, dto);
    }

    @PatchMapping("/{id}/vineland")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Registrar puntuaciones Vineland")
    public PacienteResponseDTO updateVineland(@PathVariable Long id,
                                              @RequestBody PacienteVinelandDTO dto) {
        return service.updateVineland(id, dto);
    }

    @PatchMapping("/{id}/segunda-visita")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'CUERPO_TECNICO')")
    @Operation(summary = "Registrar fecha de extracción de sangre (segunda visita)")
    public PacienteResponseDTO updateSegundaVisita(@PathVariable Long id,
                                                   @RequestBody @Valid PacienteSegundaVisitaDTO dto) {
        return service.updateSegundaVisita(id, dto);
    }

    @PostMapping("/{id}/reenviar-mchat")
    @PreAuthorize("hasAnyRole('INVESTIGADOR_PRINCIPAL', 'CUERPO_MEDICO', 'ROTANTE_CLINICA')")
    @Operation(summary = "Regenerar y reenviar el enlace M-CHAT por mail")
    public PacienteResponseDTO reenviarMchat(@PathVariable Long id) {
        return service.reenviarMchat(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
    @Operation(summary = "Dar de baja paciente (baja lógica)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
