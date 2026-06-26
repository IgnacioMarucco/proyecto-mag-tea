package com.utn.magtea.storage;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties props;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(props.getUrl())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

    /**
     * Cliente para generar presigned URLs.
     * Usa la URL pública para que el browser pueda verificar la firma.
     * La generación de presigned URLs es local (sin conexión real a MinIO).
     */
    @Bean
    @Qualifier("presigned")
    public MinioClient presignedMinioClient() {
        String endpoint = (props.getPublicUrl() != null && !props.getPublicUrl().isBlank())
                ? props.getPublicUrl()
                : props.getUrl();
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }
}
