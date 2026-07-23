# ADR-0025: Filtros de lista con soporte multi-select por grupo

## Contexto

Las vistas de bandeja y pacientes necesitan filtrar por múltiples valores de estado simultáneamente (ej: ver PENDIENTE + CONTACTADO al mismo tiempo, ocultando DESCARTADO por defecto). Un filtro radio solo permite un valor a la vez.

## Decisión

`FilterGroup` tiene un flag `multiSelect?: boolean`. Cuando es `true`, el grupo usa checkboxes y `activeFilters` almacena `string[]` para ese grupo. Cuando es `false` (default), mantiene `string`.

El estado inicial de `activeFilters` lo define el componente padre — el toolbar no impone defaults de negocio.

```typescript
// Bandeja: ocultar DESCARTADO y ADMITIDO por defecto
activeFilters = signal<Record<string, string | string[]>>({
  estado: ['PENDIENTE', 'CONTACTADO']
});
```

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Radio con opción "Sin descartados"** | Requiere inventar una opción artificial que no es un estado real del dominio |
| **`defaultSelected` en `FilterGroup`** | Mezcla configuración de estructura con estado de negocio — el padre es el responsable |
| **Multi-select solo para estado** | Innecesariamente inconsistente — el flag permite aplicarlo donde corresponda |

## Consecuencias

**Positivas:**
- El toolbar es agnóstico al dominio — el padre controla el estado inicial según reglas de negocio
- Pills activos reflejan correctamente selecciones individuales o resumidas (N estados ×)
- "Todas seleccionadas = sin filtro" evita mostrar pill cuando no hay filtro real activo

**Negativas / trade-offs:**
- El tipo `Record<string, string | string[]>` requiere verificar `Array.isArray()` al leer el valor en el computed de filtrado
