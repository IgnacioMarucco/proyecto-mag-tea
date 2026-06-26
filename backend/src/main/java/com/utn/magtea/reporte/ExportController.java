package com.utn.magtea.reporte;

import com.utn.magtea.common.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping(ApiConstants.V1 + "/exportar")
@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
@Tag(name = "Exportación", description = "Descarga de datos estructurados para análisis estadístico (PRISM / Excel)")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    @GetMapping("/ratones")
    @Operation(summary = "Exportar datos de modelos animales en CSV (1 fila = 1 ratón)")
    public ResponseEntity<byte[]> exportarRatones() {
        return csvResponse(exportService.exportarRatones(), "ratones_" + hoy() + ".csv");
    }

    @GetMapping("/pacientes")
    @Operation(summary = "Exportar datos clínicos de pacientes en CSV (1 fila = 1 paciente)")
    public ResponseEntity<byte[]> exportarPacientes() {
        return csvResponse(exportService.exportarPacientes(), "pacientes_" + hoy() + ".csv");
    }

    @GetMapping("/pool-composicion")
    @Operation(summary = "Exportar composición de pools en CSV (tabla puente pool ↔ paciente)")
    public ResponseEntity<byte[]> exportarPoolComposicion() {
        return csvResponse(exportService.exportarPoolComposicion(), "pool_composicion_" + hoy() + ".csv");
    }

    @GetMapping("/ratones/xlsx")
    @Operation(summary = "Exportar datos de modelos animales en XLSX (Excel, columnas ya definidas)")
    public ResponseEntity<byte[]> exportarRatonesXlsx() {
        return xlsxResponse(exportService.exportarRatonesXlsx(), "ratones_" + hoy() + ".xlsx");
    }

    @GetMapping("/pacientes/xlsx")
    @Operation(summary = "Exportar datos clínicos de pacientes en XLSX (Excel, columnas ya definidas)")
    public ResponseEntity<byte[]> exportarPacientesXlsx() {
        return xlsxResponse(exportService.exportarPacientesXlsx(), "pacientes_" + hoy() + ".xlsx");
    }

    @GetMapping("/pool-composicion/xlsx")
    @Operation(summary = "Exportar composición de pools en XLSX (Excel, columnas ya definidas)")
    public ResponseEntity<byte[]> exportarPoolComposicionXlsx() {
        return xlsxResponse(exportService.exportarPoolComposicionXlsx(), "pool_composicion_" + hoy() + ".xlsx");
    }

    @GetMapping("/completo/xlsx")
    @Operation(summary = "Exportar todos los datos en un único XLSX con 3 sheets (Ratones, Pacientes, Pool Composición)")
    public ResponseEntity<byte[]> exportarCompletoXlsx() {
        return xlsxResponse(exportService.exportarCompletoXlsx(), "magtea_completo_" + hoy() + ".xlsx");
    }

    private ResponseEntity<byte[]> csvResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    private ResponseEntity<byte[]> xlsxResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    private String hoy() {
        return LocalDate.now().format(DATE_FMT);
    }
}
