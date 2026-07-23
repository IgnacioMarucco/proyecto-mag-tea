# ADR-0016: PATCH separado para transiciones de estado

## Contexto

Entidades con ciclo de vida definido (FormularioInteres: PENDIENTE → CONTACTADO → ADMITIDO / DESCARTADO) necesitan un mecanismo para cambiar estado que respete las reglas de transición válidas.

## Decisión

Se usa un endpoint `PATCH /{id}/estado` separado del `PUT` de actualización completa. El Service valida que la transición sea válida antes de aplicarla.

**Caso especial — ADMITIDO**: esta transición no se expone en el PATCH. Solo la puede activar `PacienteService` internamente al crear un paciente, garantizando que siempre venga acompañada de la creación del registro clínico.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **`estado` en el cuerpo del PUT** | Permite transiciones arbitrarias saltándose las reglas de negocio |
| **Endpoints por acción** (`/contactar`, `/admitir`) | Verboso, multiplica endpoints, no es RESTful estricto |

## Consecuencias

**Positivas:**
- Reglas de transición centralizadas en el Service, no dispersas
- Separación clara: editar datos del formulario y avanzar su estado son responsabilidades distintas
- La transición ADMITIDO es atómica: siempre viene con creación de Paciente

**Negativas / trade-offs:**
- Más endpoints en el Controller (PUT + PATCH)
- El cliente debe saber qué endpoint usar según la operación
