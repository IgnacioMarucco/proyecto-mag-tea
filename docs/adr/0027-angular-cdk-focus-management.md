# ADR-0027: Adoptar `@angular/cdk` para focus management en modales

**Fecha:** 2026-06-13
**Estado:** Aceptado

---

## Contexto

El `ConfirmModalComponent` y el `ModalContainerComponent` (usado por los modales de CARS, Vineland y otros) no atrapaban el foco del teclado cuando estaban abiertos. Un usuario que navegaba con `Tab` podía salir del modal y moverse por elementos del fondo sin verlos. Tampoco existía atajo de `Esc` para cerrar.

Para cumplir con WCAG 2.1 AA (criterio 2.1.2 — No Keyboard Trap, en sentido opuesto: el foco sí debe quedar atrapado dentro de un modal mientras esté abierto) y con la pauta de navegación por teclado definida en la skill `/a11y`, era necesario implementar focus trapping en los dos primitivos de modal del sistema.

Se evaluaron dos enfoques:

---

## Alternativas consideradas

### A — `@angular/cdk` con `cdkTrapFocus` ✅ Elegida

Instalar `@angular/cdk` (la librería oficial de Angular para primitivas de UI) y usar la directiva `cdkTrapFocus` + `cdkTrapFocusAutoCapture` en el panel del modal.

**Ventajas:**
- Cero código manual: el CDK maneja Tab, Shift+Tab, captura automática del foco y casos edge (elementos deshabilitados, `tabindex=-1`, etc.).
- `@angular/cdk` es parte del ecosistema oficial de Angular, con la misma cadencia de versiones — sin riesgo de incompatibilidad.
- Abre la puerta a otras primitivas del CDK en el futuro: `LiveAnnouncer` (para `aria-live` centralizado), overlays, drag & drop.

**Desventajas:**
- Nueva dependencia en el proyecto.

### B — Implementación manual con JavaScript

Implementar en cada modal un `effect()` que al abrirse enumera los elementos focusables con `querySelectorAll`, mueve el foco, y agrega un listener de `keydown` para interceptar Tab/Shift+Tab y Esc.

**Ventajas:**
- Sin dependencias externas.

**Desventajas:**
- ~40 líneas adicionales de código frágil que hay que mantener.
- Difícil de cubrir todos los edge cases correctamente (elementos que se vuelven deshabilitados dinámicamente, shadow DOM, etc.).
- Duplicación si hay más modales en el futuro.

---

## Decisión

Se adopta `@angular/cdk@^21.2.14` (alineado con la versión de Angular del proyecto: `^21.2.0`).

La directiva `cdkTrapFocus cdkTrapFocusAutoCapture` se aplica al panel del modal en `ConfirmModalComponent` y `ModalContainerComponent`. El `Esc` se maneja con `(keydown.escape)` en el mismo panel. La restauración del foco al elemento que disparó el modal se implementa con `document.activeElement` en `ngOnInit` / `ngOnDestroy`, sin necesidad de CDK adicional.

Al aplicar el fix en los dos primitivos compartidos, todos los modales del sistema quedan cubiertos sin tocar cada sección clínica individualmente.

---

## Consecuencias

- `@angular/cdk` queda disponible para todo el proyecto. Si en el futuro se necesita `LiveAnnouncer`, overlays o drag & drop, no hay que agregar una dependencia nueva.
- Los modales cumplen con WCAG 2.1 AA criterio 2.1.2 y con el pattern de "Modal Dialog" de WAI-ARIA Authoring Practices.
- El tamaño del bundle aumenta levemente por la incorporación del CDK, compensado por el tree-shaking que solo incluye los módulos usados.
