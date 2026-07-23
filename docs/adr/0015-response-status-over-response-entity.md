# ADR-0015: @ResponseStatus en lugar de ResponseEntity como estilo base

## Contexto

En Spring MVC hay dos formas de controlar el HTTP status code de una respuesta. El proyecto necesita un estilo consistente que minimice el ruido en los Controllers.

## Decisión

Se usa `@ResponseStatus` sobre el método como estilo base. El método devuelve el objeto directamente, sin envolver en `ResponseEntity`.

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public PacienteResponseDTO create(@RequestBody @Valid PacienteCreateDTO dto) {
    return service.create(dto);
}
```

**Excepción**: se usa `ResponseEntity` cuando el status es dinámico, se necesitan headers de respuesta (ej: `Location`), o una respuesta con body vacío y headers específicos.

## Alternativas consideradas

**`ResponseEntity<T>` en todos los métodos** — descartado por ser más verboso sin agregar valor cuando el status es fijo.

## Consecuencias

**Positivas:**
- Firmas de método más limpias y expresivas
- Menos ruido en el Controller
- Convención clara: `@ResponseStatus(CREATED)` en POST, `@ResponseStatus(NO_CONTENT)` en DELETE

**Negativas / trade-offs:**
- `@ResponseStatus` fija el código en compilación — no permite lógica condicional de status
- Cuando se necesita `ResponseEntity`, la mezcla de estilos en el mismo Controller puede ser inconsistente visualmente
