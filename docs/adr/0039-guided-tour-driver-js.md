# ADR-0039: Guided tour interactivo con Driver.js

**Estado:** Aceptado  
**Fecha:** 2026-07-01  
**Relacionado con:** —

## Contexto

El portal interno (profesionales clínicos y técnicos) tiene múltiples pantallas de gestión (Bandeja, Pacientes, Sueros, Pools, Modelos Animales, Profesionales, Cajas, Camadas). Se identificó la necesidad de un mecanismo de onboarding contextual que explique qué hace cada sección de cada pantalla sin requerir documentación externa.

Los requisitos eran:
- Un único botón de entrada siempre en la misma posición (no dentro de cada pantalla)
- Fácil de extender a pantallas nuevas con el mínimo de código
- Integrable con Angular 21 sin un wrapper específico del framework
- UI que no desentonara con el design system del proyecto

## Decisión

Se implementó un sistema de guided tour usando **Driver.js v1.x** con el siguiente diseño:

### Arquitectura

```
core/tour-steps.ts          ← datos puros: steps por ruta
core/services/tour.service  ← lógica: instancia Driver.js, filtra steps sin elemento en DOM
internal/layout/             ← botón "Ayuda" en el sidebar (posición fija)
[cada list component].html   ← atributos data-tour en los elementos a resaltar
```

### Patrón de extensión

Para agregar el tour a una pantalla nueva solo se requieren dos cambios:

1. Agregar atributos `data-tour="nombre-elemento"` a los elementos clave en el template
2. Agregar una entry en `tour-steps.ts` con la key `/internal/ruta` y el array de pasos

No hay cambios al `TourService`, al sidebar ni a ningún otro archivo.

### Ubicación del botón

El botón de Ayuda vive en el sidebar (sección footer, encima del email de usuario), no dentro de cada pantalla. Esto garantiza posición invariante independientemente de qué pantalla está activa. Driver.js puede resaltar cualquier elemento del DOM desde cualquier trigger, por lo que el botón no necesita estar en la misma pantalla que los elementos resaltados.

### Filtrado de elementos condicionales

El `TourService` filtra pasos cuyo elemento no existe en el DOM antes de iniciar el tour:

```typescript
const visible = steps.filter(s => !s.element || !!document.querySelector(s.element as string));
```

Esto maneja graciosamente elementos condicionales (como el paginador cuando hay menos de 20 ítems).

### Estilos

Los botones de navegación del tour (Siguiente, Anterior, Listo) se sobreescriben en `styles.css` para usar las variables CSS del proyecto (`--color-primary`, `--color-border`, etc.), eliminando el estilo por defecto de Driver.js que no pegaba con el design system.

## Alternativas evaluadas

| Alternativa | Motivo de descarte |
|---|---|
| **ngx-joyride** | Wrapper Angular-nativo con directivas, pero poco mantenido y compatibilidad con Angular 21 no verificada |
| **Shepherd.js** | API más verbosa, mayor complejidad de setup sin ventajas claras para este caso |
| **Intro.js** | Popular pero requiere wrapper para Angular; API más antigua |
| **Driver.js** ✓ | Vanilla JS, API limpia, activamente mantenido (v1.x reescritura completa), integración sin friction con Angular |

## Pantallas cubiertas

| Pantalla | Pasos | Elementos únicos |
|---|---|---|
| Bandeja | 4 | — |
| Pacientes | 5 | Botón "Registrar paciente" |
| Sueros | 6 | Botón "Crear pool" con tooltip condicional |
| Pools | 7 | Botón "Crear ratón", matriz de disponibilidad de sueros |
| Modelos Animales | 5 | — |
| Profesionales | 5 | — |
| Cajas | 5 | — |
| Camadas | 5 | — |

## Consecuencias

- Agregar tour a una pantalla nueva cuesta ~10 líneas de código (steps en `tour-steps.ts` + `data-tour` en el template)
- El paginador se omite automáticamente en pantallas con pocos registros, sin lógica per-step
- Driver.js es una dependencia de producción (~20kb gzipped); el CSS se importa en `angular.json`
- Si se elimina una pantalla o se renombra una ruta, hay que actualizar la key correspondiente en `tour-steps.ts`
