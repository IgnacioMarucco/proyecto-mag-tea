# ADR-0026: Sistema de notificaciones toast centralizado

**Estado:** Aceptado

---

## Contexto

El portal interno realiza múltiples operaciones con feedback inmediato esperado: guardar un formulario, dar de baja un profesional, registrar un estudio en un modelo animal. El usuario necesita confirmación visual de que la operación fue exitosa (o qué salió mal).

Las opciones habituales son: alerts del navegador, mensajes inline en el formulario, snackbars materiales, o un sistema de toasts global.

---

## Decisión

Se usa un **`ToastService` singleton** (signal-based) con un **`ToastContainerComponent`** renderizado una vez en el layout del portal interno.

```typescript
// ToastService
show(message: string, type: 'success' | 'error' = 'success', duration = 3500): void {
  const id = Date.now();
  this._toasts.update(t => [...t, { id, message, type }]);
  setTimeout(() => this.dismiss(id), duration);
}
```

- **Tipos**: `success` (verde) y `error` (rojo)
- **Duración por defecto**: 3.5 segundos, descartable manualmente
- **Posición**: esquina superior derecha (fija en viewport)
- **Estado**: `signal<Toast[]>` — `ToastContainerComponent` usa `OnPush` y lee el signal directamente

Los componentes que necesitan notificar inyectan `ToastService` y llaman a `toastService.show(message, type)` tras operaciones exitosas o en `catchError`.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Alerts del navegador (`window.alert`)** | Bloquean el hilo, no son estilizables, mala UX |
| **Mensajes inline en el formulario** | Solo son visibles en la pantalla del formulario; si el usuario navega tras guardar, no ve el resultado |
| **Angular Material Snackbar** | Requiere `@angular/material` como dependencia completa — overkill para una sola primitiva |
| **Banner en el componente padre** | Requiere coordinar estado entre componente hijo (form) y padre (layout) — más complejo que un servicio global |

## Consecuencias

**Positivas:**
- Un servicio inyectable en cualquier componente — no requiere coordinar entre padre e hijo
- Al estar basado en signals, el `ToastContainerComponent` con `OnPush` detecta cambios sin zone.js
- La implementación es self-contained: no hay dependencia externa

**Negativas / trade-offs:**
- Solo dos tipos de notificación (`success` / `error`) — si en el futuro se necesitan `warning` o `info`, hay que extender el tipo y el estilo
- Los toasts se auto-descartan: si el usuario no los ve en 3.5 segundos, se pierden sin historial
