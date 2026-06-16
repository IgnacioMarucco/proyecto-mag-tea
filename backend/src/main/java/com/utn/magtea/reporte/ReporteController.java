package com.utn.magtea.reporte;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.reporte.dto.CarsAnaliticaDTO;
import com.utn.magtea.reporte.dto.CorrelacionPuntoDTO;
import com.utn.magtea.reporte.dto.DemograficoDTO;
import com.utn.magtea.reporte.dto.EmbudoDTO;
import com.utn.magtea.reporte.dto.MchatAnaliticaDTO;
import com.utn.magtea.reporte.dto.ResumenGeneralDTO;
import com.utn.magtea.reporte.dto.VinelandAnaliticaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/reportes")
@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
@Tag(name = "Reportes", description = "Estadísticas de la cohorte clínica")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/resumen")
    @Operation(summary = "KPI cards del dashboard")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResumenGeneralDTO getResumen() {
        return reporteService.getResumen();
    }

    @GetMapping("/embudo")
    @Operation(summary = "Funnel de reclutamiento por etapa del protocolo")
    @ApiResponse(responseCode = "200", description = "OK")
    public EmbudoDTO getEmbudo() {
        return reporteService.getEmbudo();
    }

    @GetMapping("/demografico")
    @Operation(summary = "Distribución por sexo y fuente de derivación")
    @ApiResponse(responseCode = "200", description = "OK")
    public DemograficoDTO getDemografico() {
        return reporteService.getDemografico();
    }

    @GetMapping("/mchat")
    @Operation(summary = "Análisis M-CHAT — scores, resultado final, ítems fallados")
    @ApiResponse(responseCode = "200", description = "OK")
    public MchatAnaliticaDTO getMchat() {
        return reporteService.getMchat();
    }

    @GetMapping("/cars")
    @Operation(summary = "Análisis CARS-2 — distribución de scores y categorías")
    @ApiResponse(responseCode = "200", description = "OK")
    public CarsAnaliticaDTO getCars() {
        return reporteService.getCars();
    }

    @GetMapping("/vineland")
    @Operation(summary = "Perfil Vineland — medias por subdominio")
    @ApiResponse(responseCode = "200", description = "OK")
    public VinelandAnaliticaDTO getVineland() {
        return reporteService.getVineland();
    }

    @GetMapping("/correlaciones")
    @Operation(summary = "Datos de scatter para cruces entre escalas")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<CorrelacionPuntoDTO> getCorrelaciones(
            @RequestParam(defaultValue = "MCHAT_SCORE") String ejeX,
            @RequestParam(defaultValue = "CARS_RAW")    String ejeY) {
        return reporteService.getCorrelaciones(ejeX, ejeY);
    }
}
