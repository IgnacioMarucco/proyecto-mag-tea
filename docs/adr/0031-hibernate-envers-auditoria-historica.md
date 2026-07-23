# ADR-0031: Hibernate Envers para auditoría histórica de entidades clínicas

## Contexto

El sistema MAG-TEA maneja datos clínicos de menores de edad (diagnósticos, resultados de evaluaciones neurológicas, sueros). La validez científica del estudio requiere trazabilidad completa: saber quién modificó qué dato y cuándo, y poder reconstruir el estado histórico de cualquier registro.

`Auditable` ya provee `createdAt`, `updatedAt` y `createdBy`, pero `updatedAt` se sobreescribe en cada cambio — no hay historial. Si un médico corrige el raw score de CARS-2 tres veces, solo se conserva el último valor.

También existe el reporte R-CLI-07 (embudo de reclutamiento) que requiere saber cuántos días permaneció un formulario en cada estado, información que `updated_at` no puede reconstruir.

Las alternativas evaluadas fueron:

1. **Tabla de auditoría genérica** (`audit_log` con `tabla`, `campo`, `valor_anterior`, `valor_nuevo`): pierde tipos, difícil de reconstruir estado completo en un momento dado.
2. **Tablas de historial por entidad** (mirror tables a mano por cada entidad): más código, misma funcionalidad.
3. **Hibernate Envers**: librería estándar del ecosistema JPA/Hibernate; agrega `@Audited` a la entidad y genera tablas `*_aud` automáticamente con snapshot completo en cada revisión.

## Decisión

Se usa **Hibernate Envers** para auditoría histórica de todas las entidades clínicas.

`Auditable` y Envers coexisten y cubren necesidades distintas:

| | `Auditable` | Envers |
|---|---|---|
| `createdAt` / `createdBy` | ✅ Campo directo, disponible en respuestas de API | ✅ Primera revisión |
| `updatedAt` | ✅ Útil para cache y displays | ✅ Revisión más reciente |
| `lastModifiedBy` | ✅ Agregado en este ADR | ✅ Revisión más reciente |
| Historial completo | ❌ | ✅ |
| Costo de consulta | Cero (campo en tabla principal) | Query a tabla `_aud` |

`Auditable` no se elimina: sus campos son operacionales y aparecen directamente en respuestas de API sin requerir queries adicionales. Se le agrega `lastModifiedBy` con `@LastModifiedBy` para completar el registro de "quién tocó esto por última vez".

Entidades auditadas: `FormularioInteres`, `Paciente`, `PacienteCriterios`, `PacienteEvaluacionCars`, `PacienteEvaluacionVineland`, `PacienteMchatSeguimiento`, `MchatRespuestas`, `Profesional`.

No auditadas: `Donacion` (sin valor científico ni clínico).

### Nota de implementación — Hibernate 6

En Hibernate ORM 6 (incluido en Spring Boot 4.x), `DefaultRevisionEntity` es `final` y no puede extenderse. La `CustomRevisionEntity` debe definir sus propios campos con `@RevisionNumber` y `@RevisionTimestamp` en lugar de heredar de `DefaultRevisionEntity`.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Audit log genérico** | Todos los valores como `TEXT`, pierde tipos, reconstruir estado completo requiere replay |
| **Tablas de historial manuales por entidad** | Código duplicado por entidad, misma funcionalidad que Envers con más mantenimiento |
| **Solo `Auditable` con `updatedAt`** | No hay historial — la información de cambios intermedios se pierde para siempre |

## Consecuencias

**Positivas:**
- Una anotación `@Audited` por entidad cubre toda la trazabilidad clínica
- El investigador principal puede reconstruir el estado de cualquier paciente en cualquier fecha
- El `RevisionListener` captura el usuario autenticado automáticamente desde el `SecurityContext`; cuando el cambio es anónimo (familia completando el M-CHAT por token), `usuario` queda `null` — comportamiento correcto e intencional
- Agnóstico al motor de base de datos (funciona con PostgreSQL, MySQL, H2, etc.)

**Negativas / trade-offs:**
- Agrega una tabla `*_aud` por cada entidad auditada y una tabla central `revinfo`
- Las consultas de historial requieren la API `AuditReader` de Envers, no SQL puro
- Los snapshots completos ocupan más espacio que guardar solo deltas
