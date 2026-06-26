package com.utn.magtea.storage;

import jakarta.validation.constraints.NotBlank;

public record PresignedUploadRequestDTO(
        @NotBlank(message = "El bucket es obligatorio") String bucket,
        @NotBlank(message = "El nombre del archivo es obligatorio") String nombreOriginal,
        String mimeType,
        Long tamanio
) {}
