# ADR-0035: `@defer (when condición)` para lazy loading de secciones clínicas en el detalle de paciente

**Fecha:** 2026-06-13
**Estado:** Aceptado

---

## Contexto

`paciente-detail.component.html` carga seis secciones clínicas en simultáneo al abrir el detalle de un paciente:

| Sección | Peso | Condición de negocio |
|---|---|---|
| `datos-basicos-section` | Liviana | Siempre visible |
| `mchat-section` | Media | Solo pacientes caso-problema |
| `resultado-mchat-section` | Media | Solo si el M-CHAT requiere seguimiento |
| `cars-section` | **Pesada** (15 ítems + scoring) | Requiere criterios cumplidos |
| `vineland-section` | **Pesada** (modal con evaluación) | Requiere criterios cumplidos |
| `extraccion-section` | Media | Siempre visible al final de la página |

En la mayoría de los casos clínicos, el paciente no tiene criterios registrados aún al momento de la primera visita al detalle, por lo que `cars-section` y `vineland-section` no se muestran. Sin `@defer`, sus bundles se descargaban igualmente, afectando el LCP inicial.

Se evaluaron dos estrategias de deferimiento:

---

## Alternativas consideradas

### A — `@defer (on viewport)` para todas las secciones pesadas

Las secciones se cargan cuando el usuario hace scroll y el elemento entra en pantalla.

**Ventajas:**
- Uniforme y simple: una sola estrategia para todas.
- `cars-section` y `vineland-section` no cargan hasta que el usuario hace scroll, independientemente de los criterios.

**Desventajas:**
- `cars-section` y `vineland-section` cargan aunque el paciente no cumpla criterios (en cuanto el div placeholder entra al viewport), desperdiciando el bundle.
- No captura la semántica del dominio: la razón por la que esas secciones no se muestran es una condición clínica, no la posición en el scroll.

### B — `@defer (when condición)` alineado con las reglas de negocio ✅ Elegida

Cada sección pesada difiere su carga hasta que se cumpla la condición clínica que la habilita. `extraccion-section` (siempre presente, al final) usa `on viewport`.

| Sección | Estrategia |
|---|---|
| `resultado-mchat-section` | `@defer (when requiereSeguimiento())` |
| `cars-section` | `@defer (when criterioCumplido())` |
| `vineland-section` | `@defer (when criterioCumplido())` |
| `extraccion-section` | `@defer (on viewport)` |

**Ventajas:**
- Si el paciente no cumple criterios, los bundles de CARS y Vineland **nunca se descargan** en esa sesión.
- La condición del `@defer` documenta explícitamente en el template cuándo tiene sentido cargar cada sección — cohesión entre la lógica de negocio y la carga de recursos.
- Se pueden combinar triggers (`on viewport; when condición`) en el futuro sin cambiar la lógica.

**Desventajas:**
- Ligera variación entre secciones (no todos usan el mismo trigger) — mitigado porque el template es legible y cada `@defer` tiene su condición explícita.

---

## Decisión

Se aplica la Alternativa B. Los `@if` existentes se mantienen como control de visibilidad en el DOM; los `@defer` dentro de ellos agregan el beneficio de code-splitting de bundles. Cada `@defer` lleva un `@placeholder` con un skeleton `animate-pulse` del tamaño aproximado de la sección para evitar saltos de layout (CLS) mientras el chunk carga.

`datos-basicos-section` y `mchat-section` quedan sin `@defer` porque son las primeras secciones visibles al abrir el detalle — diferirlas empeoraría el LCP.

---

## Consecuencias

- El bundle principal del portal interno es más pequeño: los chunks de CARS, Vineland y seguimiento M-CHAT se cargan bajo demanda.
- Para pacientes en etapa `ADMITIDO` (sin criterios aún), la carga inicial del detalle es significativamente más liviana.
- Si en el futuro se agregan más secciones clínicas al detalle del paciente, deben evaluarse con la misma lógica: ¿tiene una condición de negocio natural? → `when`. ¿Siempre está presente pero al final? → `on viewport`.
