package com.utn.magtea.reporte;

import com.utn.magtea.common.ApiConstants;
import com.utn.magtea.reporte.dto.CorrelacionResponseDTO;
import com.utn.magtea.reporte.dto.DashboardAnaliticaDTO;
import com.utn.magtea.reporte.dto.DonacionesAnaliticaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/reportes")
@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
@Tag(name = "Reportes", description = "Estadísticas de la cohorte clínica")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard analítico completo: resumen, embudo, demografía, M-CHAT, CARS y Vineland en un solo request")
    @ApiResponse(responseCode = "200", description = "OK")
    public DashboardAnaliticaDTO getDashboard() {
        return reporteService.getDashboard();
    }

    @GetMapping("/correlaciones")
    @Operation(summary = "Datos de scatter para un par de ejes seleccionado, con coeficiente de Pearson")
    @ApiResponse(responseCode = "200", description = "OK")
    public CorrelacionResponseDTO getCorrelaciones(
            @RequestParam(defaultValue = "MCHAT_SCORE") String ejeX,
            @RequestParam(defaultValue = "CARS_RAW")    String ejeY) {
        return reporteService.getCorrelaciones(ejeX, ejeY);
    }

    @GetMapping("/donaciones")
    @Operation(summary = "Recaudación por mes y distribución de donaciones por estado")
    @ApiResponse(responseCode = "200", description = "OK")
    public DonacionesAnaliticaDTO getDonaciones() {
        return reporteService.getDonaciones();
    }
}
