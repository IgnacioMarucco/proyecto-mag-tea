package com.utn.magtea.storage;

import com.utn.magtea.common.exception.ResourceNotFoundException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    public static final String BUCKET_CONSENTIMIENTOS = "consentimientos";
    public static final String BUCKET_MICROSCOPIA = "microscopia";

    private final MinioClient minioClient;

    @Qualifier("presigned")
    private final MinioClient presignedMinioClient;

    private final MinioProperties props;
    private final DocumentoRepository documentoRepository;

    @PostConstruct
    public void initBuckets() {
        for (String bucket : List.of(BUCKET_CONSENTIMIENTOS, BUCKET_MICROSCOPIA)) {
            try {
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            } catch (Exception e) {
                log.warn("No se pudo inicializar el bucket '{}' en MinIO: {}", bucket, e.getMessage());
            }
        }
    }

    @Transactional
    public PresignedUploadResponseDTO generarPresignedUpload(PresignedUploadRequestDTO request) {
        String safeFilename = request.nombreOriginal().replaceAll("[^a-zA-Z0-9._-]", "_");
        String clave = UUID.randomUUID() + "/" + safeFilename;

        String uploadUrl;
        try {
            uploadUrl = presignedMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(request.bucket())
                            .object(clave)
                            .region("us-east-1")
                            .expiry(props.getPresignedExpiryMinutes(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error generando URL de subida", e);
        }

        Documento documento = new Documento();
        documento.setBucket(request.bucket());
        documento.setClave(clave);
        documento.setNombreOriginal(request.nombreOriginal());
        documento.setMimeType(request.mimeType());
        documento.setTamanio(request.tamanio());
        Documento saved = documentoRepository.save(documento);

        return new PresignedUploadResponseDTO(
                toDTO(saved),
                uploadUrl,
                LocalDateTime.now().plusMinutes(props.getPresignedExpiryMinutes())
        );
    }

    @Transactional(readOnly = true)
    public String generarPresignedDownload(Long documentoId) {
        Documento doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento con id " + documentoId + " no existe"));

        try {
            return presignedMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(doc.getBucket())
                            .object(doc.getClave())
                            .region("us-east-1")
                            .expiry(props.getPresignedExpiryMinutes(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error generando URL de descarga", e);
        }
    }

    public DocumentoResponseDTO toDTO(Documento d) {
        return new DocumentoResponseDTO(d.getId(), d.getBucket(), d.getClave(),
                d.getNombreOriginal(), d.getMimeType(), d.getTamanio(), d.getCreatedAt());
    }
}
