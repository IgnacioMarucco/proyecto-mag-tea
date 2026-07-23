# ADR-0018: Estado del protocolo de ModeloAnimal derivado de datos, no seteado manualmente

**Estado:** Aceptado

---

## Contexto

El modelo animal (ratón inoculado con pool de sueros) atraviesa un protocolo secuencial de estudios conductuales y análisis microscópico. Las etapas son:

1. Registro del ratón y asignación a pool/camada
2. Inoculación (fecha día 1 + 4 aportes de tubos en días distintos)
3. Vocalizaciones ultrasónicas (VUS)
4. Estudio tres cámaras
5. Microscopía (conteo de células ganglionares y de Purkinje)

El sistema necesita mostrar en qué paso está cada ratón para guiar al técnico sobre qué registrar a continuación, y para filtrar la lista por estado del protocolo.

La pregunta es: ¿el técnico selecciona el estado manualmente, o el sistema lo deriva del estado de los datos?

---

## Decisión

**El `estadoProtocolo` se calcula automáticamente** a partir de los datos registrados en el `ModeloAnimal`. No es un campo que el técnico edita directamente.

```java
// ModeloAnimalService
if (m.getFechaDia1Inoculacion() == null)   return EstadoProtocolo.PENDIENTE_INOCULACION;
if (m.getAportes().size() < 4)             return EstadoProtocolo.INOCULACION_EN_CURSO;
if (m.getVocalizaciones() == null)         return EstadoProtocolo.PENDIENTE_VOCALIZACIONES;
if (m.getTresCamaras() == null)            return EstadoProtocolo.PENDIENTE_TRES_CAMARAS;
if (m.getNumCelulasGanglionares() == null) return EstadoProtocolo.PENDIENTE_MICROSCOPIA;
return EstadoProtocolo.COMPLETO;
```

**Estados del enum `EstadoProtocolo`:**

| Estado | Condición |
|---|---|
| `PENDIENTE_INOCULACION` | Sin fecha día 1 |
| `INOCULACION_EN_CURSO` | Fecha día 1 presente, menos de 4 aportes |
| `PENDIENTE_VOCALIZACIONES` | 4 aportes completos, sin registro VUS |
| `PENDIENTE_TRES_CAMARAS` | VUS registradas, sin estudio tres cámaras |
| `PENDIENTE_MICROSCOPIA` | Tres cámaras registradas, sin datos de microscopía |
| `COMPLETO` | Todos los datos presentes |

**Endpoints PATCH** que desencadenan la recalculación del estado:
- `PATCH /{id}/inoculacion` — registra fechaDia1 y aportes de tubos
- `PATCH /{id}/vocalizaciones` — registra VUS
- `PATCH /{id}/tres-camaras` — registra tres cámaras
- `PATCH /{id}/microscopia` — registra células; puede llevar al estado COMPLETO

El campo `estadoProtocolo` **se persiste** en DB (actualizado en cada PATCH), lo que permite filtrar por estado en queries SQL sin recalcular en memoria.

**`fechaProximoEvento`** se calcula desde la fecha de inoculación según el estado actual (ej: VUS: +5 días, tres cámaras: +19 días) y se persiste para mostrarla en la lista.

## Relación con ADR-0016

ADR-0016 documenta el patrón PATCH para transiciones de estado en `FormularioInteres`. `ModeloAnimal` usa el mismo patrón (endpoints PATCH separados por acción), pero su estado no lo controla el usuario directamente sino la presencia de datos — una diferencia semántica importante.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **El técnico selecciona el estado en un selector** | Riesgo de inconsistencia: puede marcar COMPLETO sin haber registrado microscopía |
| **Un único PATCH `/estado` con enum** | El técnico podría setear PENDIENTE_MICROSCOPIA sin tener VUS — el estado y los datos quedarían desincronizados |
| **`@Formula` JPA calculado al vuelo** | No persiste: no se puede filtrar en SQL ni mostrar en listados sin calcular en Java para cada fila |

## Consecuencias

**Positivas:**
- El estado siempre es coherente con los datos — no hay forma de tener `COMPLETO` con datos incompletos
- El técnico no necesita razonar sobre en qué estado está el ratón — el sistema lo indica automáticamente
- Filtrar por estado en el listado es una query simple (`WHERE estadoProtocolo = ?`)

**Negativas / trade-offs:**
- Agregar un nuevo paso al protocolo (ej: un nuevo estudio) requiere agregar una entrada al enum, actualizar la lógica de cálculo, agregar un nuevo endpoint PATCH y migrar los registros existentes al nuevo estado correspondiente
- El orden de los pasos es fijo en el código — no es configurable sin modificar la lógica de derivación
