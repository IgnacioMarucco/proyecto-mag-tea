package com.utn.magtea.storage;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock private MinioClient minioClient;
    @Mock private MinioClient presignedMinioClient;
    @Spy  private MinioProperties props = new MinioProperties();
    @Mock private DocumentoRepository documentoRepository;

    private StorageService service;

    @BeforeEach
    void setUp() {
        // Manual instantiation guarantees proper mock injection for same-type dependencies
        service = new StorageService(minioClient, presignedMinioClient, props, documentoRepository);
    }

    // ── generarPresignedUpload — validación de bucket ─────────────────────────────

    @Test
    void deberia_lanzarBusinessRuleException_cuandoBucketNoPermitido() {
        var request = new PresignedUploadRequestDTO("bucket-invalido", "foto.jpg", "image/jpeg", 1024L);

        assertThatThrownBy(() -> service.generarPresignedUpload(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Bucket no permitido");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoBucketEsNull() {
        var request = new PresignedUploadRequestDTO("otro-bucket", "doc.pdf", "application/pdf", 2048L);

        assertThatThrownBy(() -> service.generarPresignedUpload(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Bucket no permitido")
                .hasMessageContaining("consentimientos")
                .hasMessageContaining("microscopia");
    }

    // ── generarPresignedDownload — validación de existencia ────────────────────────

    @Test
    void deberia_lanzarResourceNotFoundException_cuandoDocumentoNoExiste() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generarPresignedDownload(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Documento con id 99 no existe");
    }

    // ── toDTO ─────────────────────────────────────────────────────────────────────

    @Test
    void deberia_mapearDocumentoADTO_cuandoLlamadoToDTO() {
        var doc = buildDocumento(5L, "microscopia", "uuid/img.jpg", "img.jpg");
        doc.setMimeType("image/jpeg");
        doc.setTamanio(1024L);

        var dto = service.toDTO(doc);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.bucket()).isEqualTo("microscopia");
        assertThat(dto.clave()).isEqualTo("uuid/img.jpg");
        assertThat(dto.nombreOriginal()).isEqualTo("img.jpg");
        assertThat(dto.mimeType()).isEqualTo("image/jpeg");
        assertThat(dto.tamanio()).isEqualTo(1024L);
    }

    @Test
    void deberia_mapearDocumentoADTO_cuandoCamposOpcionalesNull() {
        var doc = buildDocumento(3L, "consentimientos", "uuid/file.pdf", "file.pdf");
        // mimeType y tamanio quedan null

        var dto = service.toDTO(doc);

        assertThat(dto.mimeType()).isNull();
        assertThat(dto.tamanio()).isNull();
    }

    // ── sanitización de nombre de archivo y happy path ────────────────────────────

    @Test
    void deberia_permitirUpload_cuandoBucketConsentimientosConNombreNormal() throws Exception {
        var request = new PresignedUploadRequestDTO("consentimientos", "doc.pdf", "application/pdf", 100L);
        var doc = buildDocumento(1L, "consentimientos", "uuid/doc.pdf", "doc.pdf");

        when(presignedMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio/presigned-upload");
        when(documentoRepository.save(any(Documento.class))).thenReturn(doc);

        var result = service.generarPresignedUpload(request);
        assertThat(result.presignedUrl()).isEqualTo("https://minio/presigned-upload");
        assertThat(result.documento().id()).isEqualTo(1L);
    }

    @Test
    void deberia_permitirUpload_cuandoBucketMicroscopia() throws Exception {
        var request = new PresignedUploadRequestDTO("microscopia", "img.png", "image/png", 512L);
        var doc = buildDocumento(2L, "microscopia", "uuid/img.png", "img.png");

        when(presignedMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio/presigned-upload");
        when(documentoRepository.save(any(Documento.class))).thenReturn(doc);

        var result = service.generarPresignedUpload(request);
        assertThat(result.presignedUrl()).isEqualTo("https://minio/presigned-upload");
        assertThat(result.documento().id()).isEqualTo(2L);
    }

    @Test
    void deberia_generarPresignedDownload_cuandoDocumentoExiste() throws Exception {
        var doc = buildDocumento(1L, "consentimientos", "uuid/doc.pdf", "doc.pdf");

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(presignedMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio/presigned-download");

        var result = service.generarPresignedDownload(1L);

        assertThat(result).isEqualTo("https://minio/presigned-download");
    }

    @Test
    void deberia_lanzarRuntimeException_cuandoDocumentoExistePeroMinioFalla() throws Exception {
        var doc = buildDocumento(1L, "consentimientos", "uuid/doc.pdf", "doc.pdf");
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(doc));
        doThrow(new java.io.IOException("Minio down"))
                .when(presignedMinioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

        assertThatThrownBy(() -> service.generarPresignedDownload(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error generando URL de descarga");
    }

    @Test
    void deberia_lanzarRuntimeException_cuandoMinioFallaAlSubir() throws Exception {
        var request = new PresignedUploadRequestDTO("consentimientos", "doc.pdf", "application/pdf", 100L);
        doThrow(new java.io.IOException("Minio error"))
                .when(presignedMinioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

        assertThatThrownBy(() -> service.generarPresignedUpload(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error generando URL de subida");
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private Documento buildDocumento(Long id, String bucket, String clave, String nombre) {
        var doc = new Documento();
        doc.setId(id);
        doc.setBucket(bucket);
        doc.setClave(clave);
        doc.setNombreOriginal(nombre);
        return doc;
    }
}
