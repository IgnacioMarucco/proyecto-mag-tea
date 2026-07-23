# ADR-0021: Reactive Forms para formularios en el frontend

## Contexto

El sistema tiene formularios complejos (Paciente tiene 30+ campos, validaciones condicionales dependientes del score M-CHAT). Se necesita una estrategia de formularios que escale y sea testeable.

## Decisión

Se usan **Reactive Forms** (`FormBuilder`, `FormGroup`, `FormControl`, `Validators`) en todos los formularios. Las validaciones viven en TypeScript, nunca en el template.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Template-driven forms** | Validaciones en el template son difíciles de testear, no escala para formularios complejos |
| **Signal Forms** (experimental Angular 19) | API inestable, no recomendado para producción por el equipo de Angular |

## Consecuencias

**Positivas:**
- Validaciones en TypeScript son testeables con unit tests
- `patchValue()` permite precargar datos al editar
- `reset()` limpia el formulario al cancelar
- Escala para formularios con lógica condicional compleja

**Negativas / trade-offs:**
- Más verboso que template-driven para formularios simples de 2-3 campos
- Requiere importar `ReactiveFormsModule` en cada componente standalone
