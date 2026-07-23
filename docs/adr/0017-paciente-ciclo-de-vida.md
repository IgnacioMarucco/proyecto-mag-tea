# ADR-0017: Ciclo de vida del Paciente — máquina de estados y eventos de dominio

**Estado:** Aceptado

---

## Contexto

El `Paciente` avanza por cuatro estados clínicos a lo largo del protocolo:

```
ADMITIDO → MCHAT_RESPONDIDO → EXTRACCION_PENDIENTE → EXTRACCION_REALIZADA
```

El problema de diseño es que **las transiciones las disparan módulos diferentes**:

- El estado `MCHAT_RESPONDIDO` depende de que `MchatService` registre las respuestas de la familia.
- Los estados `EXTRACCION_PENDIENTE` y `EXTRACCION_REALIZADA` dependen de que `SueroService` registre o elimine un suero.

Si `MchatService` o `SueroService` llaman directamente a `PacienteService` para actualizar el estado, se genera un acoplamiento circular o una dependencia entre capas que no es natural al dominio.

---

## Decisión

Las transiciones de estado del Paciente se coordinan mediante **eventos de dominio** usando el `ApplicationEventPublisher` de Spring. Cada módulo publica el hecho de lo que ocurrió; `PacienteService` reacciona escuchando esos eventos.

### Eventos y sus efectos

| Evento | Publicado por | Efecto en `PacienteService` |
|--------|--------------|------------------------------|
| `MchatFamiliaGuardadaEvent` | `MchatService` al guardar respuestas públicas | Avanza a `MCHAT_RESPONDIDO`, invalida el token |
| `MchatFamiliaActualizadaEvent` | `MchatService` al editar respuestas internamente | Recalcula estado; si el score cambia y el seguimiento deja de aplicar, lo limpia |
| `SueroRegistradoEvent` | `SueroService` al crear un suero exitosamente | Avanza a `EXTRACCION_REALIZADA` |
| `SueroEliminadoEvent` | `SueroService` al hacer soft-delete de un suero | Retrocede a `EXTRACCION_PENDIENTE` |

### Lógica de derivación del estado

```java
// PacienteService.calcularEstado(Paciente p)
if (p.getEstadoClinico() == EXTRACCION_REALIZADA) return EXTRACCION_REALIZADA;
if (p.getFechaTurnoExtraccion() != null)          return EXTRACCION_PENDIENTE;
if (p.getTipoPaciente() == PROBLEMA && p.getMchatFamilia() != null) return MCHAT_RESPONDIDO;
return ADMITIDO;
```

El estado `EXTRACCION_REALIZADA` solo puede setearse vía evento `SueroRegistradoEvent`; no se recalcula desde datos porque el suero puede eliminarse y el estado debe retroceder de forma explícita.

### Estado secundario: `mchatEstado`

Existe un segundo estado calculado, `MchatEstado`, que **no se persiste**. Se computa en cada response DTO para mostrar al profesional en qué punto del flujo M-CHAT está el paciente:

| `MchatEstado` | Condición |
|---------------|-----------|
| `NO_ENVIADO` | Sin token activo ni respuestas |
| `PENDIENTE` | Token activo y no expirado |
| `EXPIRADO` | Token presente pero vencido |
| `COMPLETADO` | `MchatFamilia` registrada |

Este estado no guía la máquina de estados principal — es información de UX para el profesional.

---

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|------------|
| **`MchatService` llama a `PacienteService` directamente** | Crea dependencia de módulo bidireccional. `PacienteService` ya usa `MchatService` → dependencia circular |
| **`SueroService` llama a `PacienteService` directamente** | Introduce acoplamiento entre módulos de dominio diferente; el suero no debería "conocer" la lógica del paciente |
| **Un único endpoint PATCH `/estado` controlado por el profesional** | El técnico no debería decidir si el paciente está en `EXTRACCION_REALIZADA` — eso es un hecho del sistema cuando se registra el suero |
| **State machine library (Spring Statemachine)** | Overhead de configuración desproporcionado para 4 estados con reglas simples |

---

## Consecuencias

**Positivas:**
- `MchatService` y `SueroService` no conocen a `PacienteService` — el acoplamiento va en una sola dirección
- Las transiciones son trazables: cada evento tiene un log en `PacienteService`
- Agregar un nuevo disparador de estado solo requiere publicar un evento nuevo

**Negativas / trade-offs:**
- El flujo de una transición no es obvio leyendo un solo archivo — requiere seguir el evento desde el publicador hasta el listener
- Los eventos de Spring son síncronos por defecto y dentro de la misma transacción: si el listener falla, revierte el guardado del suero también (acoplamiento transaccional)

## Relación con ADR-0016 y ADR-0018

ADR-0016 establece el patrón PATCH separado para transiciones de estado. ADR-0018 muestra la variante de `ModeloAnimal`, donde el estado se deriva enteramente de los datos sin eventos. `Paciente` es el caso híbrido: su estado se calcula desde los datos, pero parte de esos datos los actualiza un evento externo.
