# ADR-0022: Signals para gestión de estado en el frontend

## Contexto

El frontend Angular 21 necesita un mecanismo para gestionar estado local en los componentes y reaccionar a cambios de forma eficiente con `ChangeDetectionStrategy.OnPush`.

## Decisión

Se usan **Signals** (`signal()`, `computed()`, `effect()`) como sistema principal de estado. RxJS se mantiene exclusivamente para operaciones HTTP a través del puente `toSignal()`.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **RxJS BehaviorSubject** como estado | Verboso, requiere gestionar suscripciones manualmente, propenso a memory leaks |
| **NgRx** | Overkill para este proyecto — agrega mucho código de infraestructura sin beneficio proporcional |
| **Propiedades simples de clase** | Requiere `markForCheck()` manual, sin reactividad declarativa |

## Consecuencias

**Positivas:**
- Integración nativa con `OnPush` sin configuración adicional
- `computed()` garantiza que los valores derivados nunca queden desactualizados
- Menos código que RxJS para estado local
- Base de la arquitectura zoneless de Angular 21

**Negativas / trade-offs:**
- Coexistencia con RxJS para HTTP crea un modelo híbrido — requiere entender cuándo usar cada uno
- `toSignal()` es el puente obligatorio entre ambos mundos
