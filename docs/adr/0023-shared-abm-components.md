# ADR-0023: Componentes compartidos para vistas de gestión ABM

**Estado:** Aceptado

---

## Contexto

El sistema tiene 7+ vistas de gestión (profesionales, bandeja de formularios, pacientes, sueros, pools, modelos animales, camadas, cajas) con la misma estructura: toolbar de búsqueda + filtros + tabla + badges de estado + acciones por fila + paginación. Repetir ese HTML en cada vista genera deuda de mantenimiento — un cambio de estilo (padding, color de badge) habría que replicarlo en 7+ archivos.

---

## Decisión

Se crearon componentes compartidos en `frontend/src/app/shared/`. Se organizan en dos grupos:

### Grupo 1 — Componentes del patrón ABM (toda vista de gestión los usa)

| Componente | Responsabilidad |
|---|---|
| `ListToolbarComponent` | Búsqueda + filtros por grupos + pills activos + slot CTA |
| `DataTableComponent` | Wrapper con thead configurable. Filas via `<ng-content>` |
| `StatusBadgeComponent` | Badge coloreado por valor. Colores definidos en el padre |
| `RowActionsComponent` | Botón ⋮ con dropdown `position:fixed`. Acciones pre-bindeadas al item desde el padre |
| `ConfirmModalComponent` | Modal de confirmación para baja lógica. El padre controla visibilidad y acción |
| `PaginatorComponent` | Controles de paginación server-side. Recibe `currentPage`, `totalPages`, emite `pageChange` |
| `PageHeaderComponent` | Encabezado estandarizado de vista con título, subtítulo y slot de acciones |

### Grupo 2 — Componentes de módulos especializados (usados en módulos específicos)

| Componente | Módulo que lo usa | Responsabilidad |
|---|---|---|
| `ModalContainerComponent` | Paciente (CARS, Vineland) | Contenedor genérico de modales clínicos con focus trap (ADR-0026) |
| `KpiCardComponent` | Reportes / Inicio | Card de métrica con valor, label y tendencia |
| `FreezerPickerComponent` | Suero, Tubo | Selector visual de posición en freezer (caja + posición) |
| `TuboGridComponent` | Pool, ModeloAnimal | Grilla visual de tubos disponibles por posición |
| `TuboQuantityTableComponent` | Pool | Tabla de cantidades por tubo en la composición de un pool |
| `VaciarTuboModalComponent` | Tubo | Modal para registrar vaciado de tubo |
| `CopyBadgeComponent` | General | Badge con botón de copia al portapapeles para identificadores |
| `VolumeBarComponent` | Suero, Tubo | Barra visual de volumen restante |

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Copy-paste por entidad** | Un cambio de estilo (padding, color) hay que replicarlo en 7+ archivos |
| **Tabla completamente genérica con column renderers** | Over-engineering: requiere tipado genérico complejo; las filas tienen lógica de dominio difícil de generalizar |
| **Librería de componentes externa (PrimeNG, Angular Material)** | Las entidades tienen necesidades visuales específicas (grilla de freezer, barra de volumen) que no cubre ninguna librería out-of-the-box |

## Consecuencias

**Positivas:**
- Un cambio de estilo en el toolbar o la tabla impacta todas las vistas ABM
- `DataTableComponent` delega las filas al padre via `<ng-content>` — control total sin renderers genéricos
- `RowActionsComponent` recibe acciones pre-bindeadas — no sabe nada del dominio
- Los componentes especializados (Grupo 2) encapsulan UI compleja sin que cada módulo la reimplemente

**Negativas / trade-offs:**
- `shared/` crece a medida que el sistema agrega módulos nuevos — requiere criterio para decidir si un componente es realmente reutilizable o debería vivir en su módulo
- Los componentes compartidos deben mantenerse retrocompatibles al agregar features para una entidad específica
