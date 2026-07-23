# ADR-0019: Paginación y filtrado server-side en endpoints de listado

## Contexto

El dataset de pacientes puede alcanzar ~500 registros. El DTO de Paciente es pesado (criterios de inclusión/exclusión, escalas M-CHAT/CARS/Vineland, fechas, token). Traer todos los registros en cada carga de la lista representa un problema real de performance y payload.

El filtrado por `estadoClinico` del paciente era un campo calculado en el mapper (no persistido), lo que impedía filtrarlo en la base de datos.

## Decisión

1. **Paginación server-side** en todos los endpoints `GET /api/{entidad}` con `Pageable` + `Specification` (Spring Data JPA).
2. **Filtrado server-side**: texto libre (`q`) con LIKE sobre campos clave; filtros por enum enviados como `?estados=X&estados=Y`.
3. **Ordenamiento server-side** con whitelist de campos permitidos (evita exposición de propiedades internas).
4. **Respuesta estandarizada** `PageResponse<T>` en `common/` con: `content`, `totalElements`, `totalPages`, `page`, `size`.
5. **`estadoClinico` persiste en `Paciente`**: campo `estadoClinico` guardado en DB, actualizado en cada PATCH relevante mediante `paciente.refreshEstadoClinico()`. Permite filtrar por estado en SQL sin cómputo en memoria.
6. **Frontend**: `BehaviorSubject<ListParams>` reemplaza el `BehaviorSubject<void>` — el trigger ahora carga los parámetros actuales (page, q, estados, sort). El computed de filtrado client-side se elimina. Se agrega `PaginatorComponent` compartido.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Mantener client-side filtering** | Con 500 DTOs pesados, el payload inicial es inaceptable |
| **`@Formula` JPA para estadoClinico** | Requiere SQL nativo por dialecto, frágil ante cambios de lógica |
| **Calcular estado como Specification compleja** | La lógica involucra 13+ campos booleanos — inmantenible como predicado SQL |
| **Agregar paginación solo a Paciente** | Inconsistencia en el patrón; FormularioInteres también crece |

## Consecuencias

**Positivas:**
- Payload por página: ~20 DTOs vs 500 — mejora 25× en carga inicial
- Filtrado por `estadoClinico` es correcto cross-page (no solo los 20 de la página actual)
- Patrón consistente en todos los ABMs del sistema — nuevas entidades (Suero, Pool, ModeloAnimal) nacen paginadas
- `PaginatorComponent` compartido: un solo componente mantenible para todos los listados

**Negativas / trade-offs:**
- `estadoClinico` persiste → hay que llamar `refreshEstadoClinico()` en cada PATCH que afecte el estado. Si se agrega un campo nuevo que cambie la lógica, hay que acordarse de incluirlo.
- El sort es server-side → el ordenamiento de headers en la lista ahora dispara una nueva request HTTP (antes era instantáneo en memoria). Aceptable para 500 registros.
- Tests de controller/service necesitan actualizarse para esperar `PageResponse<T>` en lugar de `List<T>`.
