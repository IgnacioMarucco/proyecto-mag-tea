package com.utn.magtea.storage;

import java.time.LocalDateTime;

public record PresignedUploadResponseDTO(
        DocumentoResponseDTO documento,
        String presignedUrl,
        LocalDateTime expiresAt
) {}
