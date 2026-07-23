# ADR-0038: Congelamiento de imagen MinIO — `RELEASE.2025-09-07T16-13-09Z`

**Estado:** Aceptado  
**Fecha:** 2026-06-28  
**Relacionado con:** ADR-0037 (MinIO como object storage)

## Contexto

En octubre de 2025, MinIO discontinuó la distribución pública de su imagen Docker en Docker Hub. La última release disponible es `RELEASE.2025-09-07T16-13-09Z`. A partir de esa fecha, la imagen `minio/minio:latest` apunta a esa release archivada y no recibirá parches de seguridad ni nuevas funcionalidades.

El problema detectado al auditar las imágenes Docker del proyecto (junio 2026) es que el `docker-compose.yml` usaba `minio/minio:latest`, que:
1. No tiene versión pinned — el comportamiento puede cambiar con un simple rebuild
2. Apunta a una release sin soporte activo

## Decisión

Se fija la imagen a `minio/minio:RELEASE.2025-09-07T16-13-09Z`, la última release estable disponible en Docker Hub.

No se migra a alternativas mantenidas (Chainguard MinIO, Quay.io) en este momento porque:
- El proyecto es académico/investigación sin datos de producción sensibles expuestos públicamente
- MinIO en este contexto maneja archivos clínicos internos (consentimientos e imágenes de microscopía) en un servidor sin acceso externo directo
- El costo de migración no justifica el beneficio a corto plazo

## Alternativas evaluadas

| Alternativa | Estado |
|---|---|
| `minio/minio:latest` | Descartado — ambiguo, sin soporte |
| `cgr.dev/chainguard/minio` | Viable, imagen mantenida con parches de seguridad. Migración futura recomendada. |
| `quay.io/minio/minio` | Viable para clientes enterprise. Requiere evaluación de licencia. |
| Reemplazar MinIO (LocalStack, Garage) | Fuera de scope — requeriría cambios en `StorageService` y el patrón de presigned URLs |

## Consecuencias

- El stack es reproducible: todos los entornos usan exactamente la misma versión
- No habrá parches de seguridad en la imagen de MinIO
- Se deberá evaluar migración a una alternativa mantenida antes del paso a producción real o si se detecta una vulnerabilidad crítica en la versión congelada

## Revisión pendiente

Evaluar migración a `cgr.dev/chainguard/minio` o equivalente antes de:
- Cualquier despliegue en servidor con acceso externo a internet
- O cuando se identifique un CVE crítico en la versión `RELEASE.2025-09-07`
