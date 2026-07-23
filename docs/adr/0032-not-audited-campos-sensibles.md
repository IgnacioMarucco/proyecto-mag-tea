# ADR-0032: @NotAudited en campos operacionales y sensibles

**Estado:** Aceptado

---

## Contexto

Hibernate Envers (ver ADR-0031) audita por defecto todos los campos de una entidad anotada con `@Audited`. Hay tres tipos de campos que no deben aparecer en las tablas `*_aud`:

1. **Campos sensibles de seguridad**: hashes de contraseñas — guardarlos en tablas de historial amplía la superficie de exposición y viola el principio de minimización de datos de la Ley 25.326 de Protección de Datos Personales (Argentina).

2. **Campos operacionales de tokens temporales**: tokens UUID de acceso con expiración — su estado histórico es nulo (un token expirado hace seis meses no tiene valor científico ni de auditoría).

3. **Campos de scheduling calculados**: fechas de próximo evento calculadas automáticamente por el sistema — representan el estado operativo actual, no una decisión clínica auditables.

---

## Decisión

Se aplica `@NotAudited` a los siguientes campos:

| Entidad | Campo | Tipo | Motivo |
|---|---|---|---|
| `Profesional` | `password` | Seguridad | Hash bcrypt — no debe persistir en historial |
| `Paciente` | `mchatToken` | Token operacional | UUID temporal sin valor histórico |
| `Paciente` | `mchatTokenExpiry` | Token operacional | Timestamp de expiración sin valor histórico |
| `Paciente` | `proximaFechaEvento` | Scheduling calculado | Fecha del próximo evento clínico esperado, recalculada automáticamente; su estado histórico no es auditables en sí mismo |

**Criterio para aplicar `@NotAudited` en el futuro:**
- Contraseñas, secrets, tokens de sesión → siempre `@NotAudited`
- Campos calculados automáticamente por el sistema (no ingresados por el usuario) cuyo historial no tenga valor clínico ni estadístico → `@NotAudited`
- Cualquier dato donde el valor pasado no ayuda a reconstruir el estado clínico del registro → `@NotAudited`

## Consecuencias

**Positivas:**
- Las tablas `*_aud` no contienen hashes de contraseñas ni tokens operacionales
- Cumplimiento del principio de minimización de datos (Ley 25.326)
- Las tablas de auditoría son más limpias: solo contienen cambios con valor de trazabilidad clínica

**Negativas / trade-offs:**
- Ninguna — estos campos no tienen valor en el historial de auditoría
