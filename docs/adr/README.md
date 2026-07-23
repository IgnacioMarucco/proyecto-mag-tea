# Architecture Decision Records — MAG-TEA

Registro de decisiones de arquitectura del proyecto. Cada decisión tiene su propio archivo numerado.

Formato basado en [Michael Nygard ADR](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) con campos: Contexto, Decisión, Alternativas consideradas, Consecuencias.

**Estados posibles:** Propuesto · Aceptado · Obsoleto · Reemplazado por ADR-XXXX

---

## Stack e infraestructura

| ADR | Título | Estado |
|-----|--------|--------|
| [0001](0001-java-21.md) | Java 21 como versión de la JVM | Aceptado |
| [0002](0002-angular-21.md) | Angular 21 como framework frontend | Aceptado |
| [0003](0003-docker-for-development.md) | Docker como entorno de desarrollo | Aceptado |
| [0004](0004-package-by-feature-backend.md) | Organización de paquetes por feature en el backend | Aceptado |
| [0005](0005-convencion-idioma-codigo.md) | Convención de idioma del código (español/inglés) | Aceptado |
| [0006](0006-mapstruct-for-entity-mapping.md) | MapStruct para mapeo entre entidades y DTOs | Aceptado |
| [0007](0007-h2-for-testing.md) | H2 en memoria como base de datos de tests | Aceptado |

## Seguridad y autenticación

| ADR | Título | Estado |
|-----|--------|--------|
| [0008](0008-jwt-authentication.md) | JWT para autenticación | Aceptado |
| [0009](0009-subdomain-routing.md) | Arquitectura de subdominios para separar portal público e interno | Aceptado |
| [0010](0010-two-layer-role-authorization.md) | Autorización en dos capas — sidebar + @PreAuthorize | Aceptado |
| [0011](0011-mchat-public-token-access.md) | Acceso público al M-CHAT mediante token UUID con expiración | Aceptado |

## Patrones de backend

| ADR | Título | Estado |
|-----|--------|--------|
| [0012](0012-java-records-for-dtos.md) | Java Records para DTOs | Aceptado |
| [0013](0013-soft-delete.md) | Soft delete en entidades de dominio médico | Aceptado |
| [0014](0014-global-exception-handler.md) | GlobalExceptionHandler con respuestas de error estandarizadas | Aceptado |
| [0015](0015-response-status-over-response-entity.md) | @ResponseStatus en lugar de ResponseEntity como estilo base | Aceptado |
| [0016](0016-patch-for-state-transitions.md) | PATCH separado para transiciones de estado | Aceptado |
| [0017](0017-paciente-ciclo-de-vida.md) | Ciclo de vida del Paciente — máquina de estados y eventos de dominio | Aceptado |
| [0018](0018-modeloanimal-estado-protocolo.md) | Estado del protocolo de ModeloAnimal derivado de datos, no seteado manualmente | Aceptado |
| [0019](0019-server-side-pagination.md) | Paginación y filtrado server-side en endpoints de listado | Aceptado |
| [0020](0020-server-assigns-contact-date.md) | El servidor asigna la fechaContacto del formulario público | Aceptado |

## Patrones de frontend

| ADR | Título | Estado |
|-----|--------|--------|
| [0021](0021-reactive-forms.md) | Reactive Forms para formularios en el frontend | Aceptado |
| [0022](0022-signals-for-frontend-state.md) | Signals para gestión de estado en el frontend | Aceptado |

## Componentes compartidos de UI

| ADR | Título | Estado |
|-----|--------|--------|
| [0023](0023-shared-abm-components.md) | Componentes compartidos para vistas de gestión ABM | Aceptado |
| [0024](0024-fixed-position-dropdown.md) | position:fixed para el dropdown de acciones por fila | Aceptado |
| [0025](0025-multiselect-filter-groups.md) | Filtros de lista con soporte multi-select por grupo | Aceptado |
| [0026](0026-toast-notification-system.md) | Sistema de notificaciones toast centralizado | Aceptado |
| [0027](0027-angular-cdk-focus-management.md) | Adoptar `@angular/cdk` para focus management en modales | Aceptado |

## Integración de pagos

| ADR | Título | Estado |
|-----|--------|--------|
| [0028](0028-checkout-pro.md) | Mercado Pago Checkout Pro (redirect) en lugar de Checkout Bricks (embebido) | Aceptado |
| [0029](0029-back-urls-primary-webhook-optional.md) | back_urls como mecanismo primario de redirect + webhook opcional | Aceptado |
| [0030](0030-external-reference-mp-payments.md) | externalReference de MP para vincular pagos con registros internos | Aceptado |

## Integridad de datos y auditoría

| ADR | Título | Estado |
|-----|--------|--------|
| [0031](0031-hibernate-envers-auditoria-historica.md) | Hibernate Envers para auditoría histórica de entidades clínicas | Aceptado |
| [0032](0032-not-audited-campos-sensibles.md) | @NotAudited en campos operacionales y sensibles | Aceptado |
| [0033](0033-persistir-rango-suero.md) | Persistir rango BTU del suero como snapshot | Aceptado |
| [0034](0034-modelo-almacenamiento-freezer.md) | Modelo de almacenamiento en freezer — Caja, Tubo y posición | Aceptado |

## Almacenamiento de archivos

| ADR | Título | Estado |
|-----|--------|--------|
| [0037](0037-minio-object-storage.md) | MinIO como object storage para archivos clínicos — consentimientos e imágenes de microscopía | Aceptado |

## Rendimiento y UX

| ADR | Título | Estado |
|-----|--------|--------|
| [0035](0035-defer-secciones-clinicas-paciente.md) | `@defer (when condición)` para lazy loading de secciones clínicas en el detalle de paciente | Aceptado |

## Reportes y exportación

| ADR | Título | Estado |
|-----|--------|--------|
| [0036](0036-export-apache-poi.md) | Exportación de datos con Apache POI (CSV y XLSX multi-hoja) | Aceptado |
