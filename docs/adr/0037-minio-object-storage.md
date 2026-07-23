# ADR-0037: MinIO como object storage para archivos clínicos — consentimientos e imágenes de microscopía

**Estado:** Aceptado

## Contexto

El sistema necesita almacenar dos tipos de archivos binarios:

1. **Documentos de consentimiento informado** (PDF firmado) — opcionales, uno por paciente.
2. **Imágenes de microscopía** (PNG/TIF de alta resolución) — opcionales, múltiples por modelo animal, con dos tipos celulares: `GANGLIONAR` y `PURKINJE`. Cada imagen pertenece a uno solo de los tipos.

Guardar estos archivos en PostgreSQL (columna `BYTEA`) fue descartado: degrada la performance de la DB, infla los backups y no escala bien para imágenes de alta resolución.

El sistema corre completamente en Docker. No se depende de servicios cloud externos.

## Decisión

### Almacenamiento: MinIO self-hosted

Se agrega un contenedor **MinIO** al `docker-compose`. MinIO expone la misma API que Amazon S3, por lo que el SDK de AWS S3 (o el SDK nativo de MinIO) es compatible. Si en el futuro el proyecto migra a S3 real, el código del backend no cambia.

Se usan dos buckets fijos, inicializados al arrancar la aplicación:
- `consentimientos` — PDFs de consentimiento
- `microscopia` — imágenes de células ganglionares y de Purkinje

### Patrón de acceso: Presigned URLs

El backend **nunca procesa los bytes del archivo**. El flujo es:

```
1. Frontend → POST /api/v1/storage/documentos  { bucket, nombreOriginal, mimeType, tamanio }
2. Backend  → crea registro Documento en DB, genera presigned PUT URL (TTL 5 min) → responde
3. Frontend → sube el archivo directamente a MinIO con la presigned PUT URL
4. Frontend → asocia el documentoId al Paciente (PATCH /consentimiento) o al ModeloAnimal (POST /imagenes-microscopia)

Para descargar:
5. Frontend → GET /api/v1/storage/documentos/{id}/presigned-download
6. Backend  → genera presigned GET URL → responde
7. Frontend → descarga directamente desde MinIO
```

Este patrón descarga el ancho de banda del servidor backend para la transferencia de archivos.

### Entidad Documento

Se agrega la entidad `Documento` en el paquete `storage/`:

```
Documento { id, bucket, clave, nombreOriginal, mimeType, tamanio, createdAt, ... }
```

La `clave` es `{UUID}/{nombreOriginalSanitizado}` — la ruta dentro del bucket en MinIO. La DB solo guarda metadatos y la referencia; el archivo vive en MinIO.

### Imágenes de microscopía

Se agrega `ImagenMicroscopia` en el paquete `modeloanimal/`:

```
ImagenMicroscopia { id, modeloAnimal, tipo (GANGLIONAR|PURKINJE), documento (nullable), urlExterna (nullable), descripcion }
```

Cada imagen debe tener exactamente un origen: un `Documento` subido a MinIO **o** una URL externa (Google Drive, servidor del laboratorio, etc.). El backend valida que al menos uno de los dos esté presente. Una imagen no puede ser de ambos tipos celulares a la vez (la discriminación es por fila, no por flag).

El paciente tiene una FK nullable `documentoConsentimiento → Documento`.

### URL pública vs. interna — patrón dual-client

MinIO corre en la red Docker interna (`http://minio:9000`). El browser no puede resolver ese hostname. La solución naive de reemplazar el host en la URL generada (`toPublicUrl()`) **no funciona**: la firma AWS4 incluye el header `Host` entre los signed headers (`X-Amz-SignedHeaders=host`), así que cambiar el dominio de la URL invalida la firma y MinIO devuelve 403.

**Solución implementada**: dos instancias de `MinioClient` en `MinioConfig`:

| Bean | Endpoint | Uso |
|------|----------|-----|
| `minioClient` (`@Primary`) | `MINIO_URL` (`http://minio:9000`) | Operaciones de bucket: `bucketExists`, `makeBucket`, etc. Conecta a MinIO desde dentro de Docker. |
| `presignedMinioClient` (`@Qualifier("presigned")`) | `MINIO_PUBLIC_URL` (`http://localhost:9000`) | Genera presigned URLs. El SDK MinIO normalmente hace una HTTP request para descubrir la región del bucket antes de firmar, lo que fallaría desde Docker con `localhost:9000`. Se evita pasando `.region("us-east-1")` explícitamente en cada `GetPresignedObjectUrlArgs`, haciendo que la generación sea puramente local. El endpoint solo determina qué host queda firmado en la URL resultante. |

El `StorageService` inyecta ambos clientes y usa `presignedMinioClient` exclusivamente para `getPresignedObjectUrl()`. Las URLs generadas ya tienen el host correcto y llegan directamente al browser sin reemplazo posterior.

| Entorno | `MINIO_URL` (backend → MinIO) | `MINIO_PUBLIC_URL` (firmado en presigned URLs) |
|---------|-------------------------------|------------------------------------------------|
| Dev | `http://minio:9000` | `http://localhost:9000` |
| Prod | `http://minio:9000` | IP o dominio público del servidor |

Para que Lombok propague `@Qualifier` al parámetro del constructor generado, se agrega en `lombok.config`:
```
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Qualifier
```

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **PostgreSQL BYTEA** | Performance degradada, backups inflados, no apto para imágenes de alta resolución |
| **Filesystem local + volumen Docker** | No escala horizontalmente, sin redundancia, sin API estandarizada |
| **AWS S3 / Cloudflare R2** | Dependencia de internet, costo, datos salen del servidor del proyecto |
| **El backend sirve los archivos directamente** | Consume ancho de banda y memoria del servidor; presigned URLs son más eficientes |
| **Campo `urlExterna` sin MinIO** | Válido para imágenes de microscopía (el lab ya tiene sus archivos en algún sistema), pero no para consentimientos que deben guardarse bajo control del proyecto |

## Consecuencias

**Positivas:**
- El backend no procesa bytes de archivos — sin pressure de memoria ni ancho de banda
- API S3-compatible: migración a S3 real es un cambio de configuración, no de código
- Los buckets se crean automáticamente al arrancar (`@PostConstruct`) — sin setup manual
- Imágenes de microscopía soportan tanto subida a MinIO como link externo en el mismo modelo
- El flujo de adjunto del consentimiento es no destructivo: el flag `consentimientoFirmado` es irreversible pero el documento puede reemplazarse independientemente
- El presigned URL para descarga (GET, TTL 5 min) evita exponer URLs permanentes a archivos clínicos

**Negativas / trade-offs:**
- Un servicio más en el `docker-compose` para operar y monitorear
- Si MinIO no está disponible al arrancar, la inicialización de buckets falla silenciosamente (log de warning) — los endpoints de storage fallarán en runtime hasta que MinIO esté disponible
- Los tests con `@SpringBootTest` arrancan con MinIO no disponible; el `@PostConstruct` logea un warning en lugar de fallar para no romper el contexto de tests
- El dual-client implica dos instancias de `MinioClient` en memoria (bajo costo, pero requiere que el `MINIO_PUBLIC_URL` esté correctamente configurado en cada entorno)
