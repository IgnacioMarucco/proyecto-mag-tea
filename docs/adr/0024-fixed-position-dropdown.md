# ADR-0024: position:fixed para el dropdown de acciones por fila

## Contexto

El `DataTableComponent` envuelve la tabla en `<div class="overflow-hidden">` — necesario para que `border-radius` recorte las esquinas. `overflow:hidden` recorta cualquier elemento con `position:absolute` que supere los límites del contenedor, incluyendo el dropdown de la última fila.

## Decisión

El dropdown de `RowActionsComponent` usa `position:fixed` con coordenadas calculadas al abrirse via `getBoundingClientRect()` sobre el botón disparador.

```typescript
const rect = button.getBoundingClientRect();
this.dropdownPos = { top: rect.bottom + 4, right: window.innerWidth - rect.right };
```

El mismo patrón aplica a cualquier dropdown dentro de contenedores con `overflow:hidden`.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Sacar `overflow-hidden`** | `border-radius` no recorta las celdas sin él — las esquinas sobresalen |
| **Portal / teleport** | Angular no tiene soporte nativo de portals tan directo, agrega complejidad |

## Consecuencias

**Positivas:**
- El dropdown sale de cualquier contexto `overflow` — funciona en la última fila de tablas largas
- Solución reutilizable para todos los dropdowns del proyecto

**Negativas / trade-offs:**
- Al hacer scroll, el dropdown queda flotando en posición incorrecta si no se cierra — se resuelve cerrándolo en el evento `scroll` del documento
- Coordenadas en pixels: si el viewport cambia mientras el dropdown está abierto (resize), puede quedar mal posicionado
