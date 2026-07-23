# ADR-0013: Soft delete en entidades de dominio médico

**Estado:** Aceptado

---

## Contexto

El sistema gestiona datos clínicos de menores de edad (diagnósticos, evaluaciones neurológicas, extracciones de sangre) y datos de investigación (sueros, pools, modelos animales). En este contexto:

- Un paciente dado de baja puede necesitar ser reactivado
- Un suero eliminado por error puede haber sido componente de un pool ya inoculado
- Los registros eliminados son potencialmente relevantes para la validez estadística del estudio

La eliminación física de registros en una base de datos clínico-científica representa un riesgo de pérdida de datos irreversible.

---

## Decisión

Todas las entidades de dominio médico tienen un campo `activo: boolean = true`. Las operaciones de "eliminación" marcan `activo = false` (baja lógica) sin tocar el registro.

Entidades con soft delete: `Paciente`, `Suero`, `Pool`, `ModeloAnimal`, `FormularioInteres`, `Camada`, `Caja`, `Profesional`.

**En los Services**: `findAll` y `findById` filtran por `activo = true`. Los endpoints `DELETE /{id}` llaman a un método del Service que setea `activo = false` y guarda.

**En el frontend**: las entidades dadas de baja desaparecen de las listas. No existe pantalla de "papelera".

**Excepción — entidades de infraestructura**: `Tubo` no tiene `activo` porque su ciclo de vida está modelado por su contenido (se vacía, no se da de baja). `Donacion` no tiene soft delete porque representa una transacción financiera — los registros financieros se mantienen por trazabilidad, no se marcan como inactivos.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Eliminación física** | Irreversible en datos clínicos; un médico puede borrar por error y no hay recuperación |
| **Tabla de archivo separada** | Requiere migración de datos al archivar y al restaurar; más complejo sin beneficio claro |
| **`deletedAt` timestamp** | Más expresivo que boolean pero requiere `IS NULL` en todas las queries — en este proyecto la semántica "activo/inactivo" es más natural que "borrado/no borrado" |

## Consecuencias

**Positivas:**
- Los datos clínicos nunca se pierden por acción del usuario
- Un profesional con acceso a la DB puede restaurar un registro seteando `activo = true`
- Hibernate Envers registra la transición `activo = false` — hay trazabilidad de quién dio de baja y cuándo (ver ADR-0031)

**Negativas / trade-offs:**
- Todas las queries de listado deben incluir `WHERE activo = true` — si se olvida en una Specification nueva, aparecen registros dados de baja
- Los IDs de entidades inactivas siguen ocupando espacio y pueden generar confusión en joins si no se filtra correctamente
