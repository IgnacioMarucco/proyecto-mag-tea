# ADR-0033: Persistir rango BTU del suero como snapshot

## Contexto

Al registrar un suero se calcula su rango según el valor de anticuerpos en BTU (Buhlmann titre unit). Los umbrales de clasificación están basados en el paper Mostafa et al. y **están pendientes de confirmación final por la investigadora principal**.

Rangos vigentes:

| Rango | BTU | Interpretación |
|-------|-----|----------------|
| Control | 0 – 1313 | Negativo |
| Rango 1 | 1314 – 2500 | Positivo leve |
| Rango 2 | 2501 – 8000 | Positivo moderado |
| Rango 3 | > 8000 | Positivo severo |

Valores de referencia del paper: cutoff 1313.5 BTU, mediana leve/moderado 1345 BTU, mediana severo 3400 BTU, valor extremo documentado 19.500 BTU.

## Decisión

El campo `rango` **se persiste** en la tabla `suero` en el momento del registro. No se recalcula al vuelo desde `valorAnticuerpos`.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| Calcular rango al vuelo desde `valorAnticuerpos` | Si la investigadora ajusta los umbrales, todos los sueros históricos cambiarían de rango retroactivamente, alterando la composición de pools ya formados |
| Campo virtual (`@Transient`) calculado en el DTO | Mismo problema: los sueros quedan "flotantes" sin rango estable en DB, y los pools no podrían filtrar por rango con queries eficientes |

## Consecuencias

**Positivas:**
- Los pools formados son estables: el rango de cada suero no cambia aunque los umbrales se revisen
- Queries por rango son simples (`WHERE rango = 2`) sin lógica de umbrales en SQL
- El historial de clasificación es auditable (rango asignado vs. valor medido)

**Negativas / trade-offs:**
- Si los umbrales cambian, los sueros nuevos se clasificarán diferente a los históricos — es intencional
- Si la investigadora decide reclasificar todo el histórico, requiere una migración de datos manual
