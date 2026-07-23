# ADR-0012: Java Records para DTOs

## Contexto

Los DTOs son objetos de transferencia de datos que se deserializan del JSON entrante o se serializan al JSON saliente. Deben ser simples, inmutables y con mínimo boilerplate.

## Decisión

Se usan **Java Records** para todos los DTOs. Son clases de datos inmutables nativas del lenguaje desde Java 16, estándar desde Java 21.

```java
public record PacienteCreateDTO(
    @NotBlank String nombreNino,
    @NotNull LocalDate fechaNacimientoNino
) {}
```

**Excepción**: ResponseDTOs con campos calculados que MapStruct no puede resolver directamente usan `@Mapping(expression = "java(...)")` — el DTO sigue siendo record.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Clase con Lombok** (`@Getter @Setter`) | Sigue siendo mutable, requiere dependencia externa para algo que Java resuelve nativamente |
| **Builder pattern manual** | Verbosísimo, Lombok con `@Builder` agrega complejidad |

## Consecuencias

**Positivas:**
- Inmutables por diseño: un DTO de request no se modifica tras deserializarse
- Compactos: 5 campos = 5 líneas, sin getters ni constructores
- Sin dependencias adicionales: Records son Java nativo
- Compatibles con Jackson, Jakarta Validation y MapStruct 1.5+

**Negativas / trade-offs:**
- Records no tienen setters — en `update()` del Service hay que actualizar campos manualmente: `entity.setCampo(dto.campo())`
- Curva de aprendizaje si se viene de clases Lombok
