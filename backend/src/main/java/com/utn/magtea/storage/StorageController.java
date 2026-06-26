package com.utn.magtea.storage;

import com.utn.magtea.common.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(ApiConstants.V1 + "/storage")
@Tag(name = "Storage")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUERPO_MEDICO', 'CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class StorageController {

    private final StorageService service;

    @PostMapping("/documentos")
    @Operation(summary = "Registrar documento y obtener URL pre-firmada para subida directa a MinIO")
    public PresignedUploadResponseDTO generarUrlSubida(@RequestBody @Valid PresignedUploadRequestDTO dto) {
        return service.generarPresignedUpload(dto);
    }

    @GetMapping("/documentos/{id}/presigned-download")
    @Operation(summary = "Obtener URL pre-firmada para descargar un documento desde MinIO")
    public Map<String, Object> generarUrlDescarga(@PathVariable Long id) {
        String url = service.generarPresignedDownload(id);
        return Map.of(
                "presignedUrl", url,
                "expiresAt", LocalDateTime.now().plusMinutes(5)
        );
    }
}
