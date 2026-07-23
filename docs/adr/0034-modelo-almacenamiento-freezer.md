# ADR-0034: Modelo de almacenamiento en freezer — Caja, Tubo y posición

**Estado:** Aceptado

---

## Contexto

Las muestras biológicas del proyecto (sueros extraídos de pacientes y pools armados para inoculación) se almacenan físicamente en tubos dentro de cajas en freezers del laboratorio. El sistema necesita:

1. Saber **dónde está** cada tubo (freezer, cajón, caja, posición dentro de la caja).
2. Saber **cuánto volumen disponible** queda en cada tubo, para validar que los aportes de pool y de inoculación no excedan lo disponible.
3. Que una misma caja pueda contener tubos de suero y tubos de pool simultáneamente.

---

## Decisión

### Entidad `Caja`

Representa una caja física dentro de un freezer. Se identifica por tres campos:

```
freezer (String) + cajón (Integer) + número (Integer)
```

El `freezer` es un código libre (ej. `"F1"`, `"PRINCIPAL"`). La combinación `(freezer, cajón, número)` tiene un unique constraint activo para evitar duplicados.

### Entidad `Tubo`

Representa un tubo físico dentro de una caja. Sus campos clave:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `caja` | FK `Caja` | Caja donde está ubicado |
| `posicion` | `String` nullable | Celda dentro de la caja, ej. `"A1"`, `"C4"` |
| `tipo` | `TipoTubo` enum | `SUERO` o `POOL` — discrimina a qué entidad pertenece |
| `suero` | FK nullable | Poblado si `tipo == SUERO` |
| `pool` | FK nullable | Poblado si `tipo == POOL` |
| `cantidadInicial` | `BigDecimal` | mL al momento de creación |
| `cantidadUsada` | `BigDecimal` | mL descontados por aportes |

`cantidadRestante` es un método calculado (`cantidadInicial - cantidadUsada`), no persistido.

### Posición como string nullable

La posición es un `String` (ej. `"A1"`) en lugar de dos columnas separadas (fila + columna). La razón es que el formato de la grilla varía entre cajas (algunas usan `A1-H12`, otras `1-81`) y la representación alfanumérica es la que usa el laboratorio en sus etiquetas.

**Cuando un tubo se vacía** (`cantidadRestante == 0`), su `posicion` se setea a `null`. Esto libera la celda en la grilla visual sin eliminar el tubo — el registro histórico permanece para auditoría. El unique constraint es sobre `(caja_id, posicion)`, y como `null` no viola unique constraints en PostgreSQL, múltiples tubos vaciados pueden coexistir sin conflicto.

### Polimorfismo por campo, no por herencia

Un tubo puede pertenecer a un suero o a un pool. Se eligió un campo `tipo` enum más dos FKs nullable sobre herencia JPA (`@Inheritance`) porque:
- Solo hay dos variantes y la lógica de negocio las trata de forma casi idéntica.
- La herencia JPA agrega complejidad de queries y mapeo que no se justifica aquí.

---

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|------------|
| **Columnas `fila` y `columna` separadas** | La representación del laboratorio es alfanumérica (`A1`); convertir en lectura y escritura agrega fricción sin beneficio |
| **Eliminar el tubo cuando se vacía** | Se pierde el historial de qué volumen tuvo y cómo se consumió, necesario para los reportes de inoculación |
| **Persistir `cantidadRestante`** | Campo derivado — persistirlo requeriría mantenerlo sincronizado manualmente; calcularlo es trivial y siempre correcto |
| **Una tabla `TuboSuero` y una `TuboPool` separadas** | Duplica la lógica de posicionamiento, ocupación y vaciado que es idéntica para ambos tipos |

---

## Consecuencias

**Positivas:**
- La grilla de ocupación de una caja se calcula en una sola query (`findByCajaIdAndSueroActivoTrue` + `findByCajaIdAndPoolActivoTrue`) sin lógica especial para los vaciados
- El historial de consumo de cada tubo está disponible para el reporte de inoculación
- Agregar un nuevo tipo de muestra solo requiere agregar un valor al enum `TipoTubo` y una FK nullable

**Negativas / trade-offs:**
- El modelo permite inconsistencias (`tipo == SUERO` pero `suero == null`); la validación es responsabilidad del `TuboService` en creación
- La posición `null` para tubos vaciados requiere filtrar activamente por `posicion IS NOT NULL` cuando se muestra la grilla, lo cual no es evidente leyendo el schema

## Relación con ADR-0033

ADR-0033 documenta la decisión de persistir el rango BTU del suero como snapshot. El `Tubo` es el vínculo físico entre el suero y el pool: los aportes de pool (`PoolSueroAporte`) referencian tubos de suero, y los aportes de inoculación (`ModeloAnimalPoolAporte`) referencian tubos de pool.
