# ADR-0014: GlobalExceptionHandler con respuestas de error estandarizadas

**Estado:** Aceptado

---

## Contexto

Sin un manejo centralizado de errores, cada controller tendría que atrapar sus propias excepciones o dejar que Spring devuelva respuestas de error con formatos variables (Whitelabel Error Page, mensajes de stack trace, etc.). El frontend necesita un contrato confiable para interpretar los errores de la API.

---

## Decisión

Se usa un único `@RestControllerAdvice` — `GlobalExceptionHandler` — que intercepta todas las excepciones de dominio y las mapea a respuestas HTTP con formato uniforme `ErrorResponse(status, error, message, timestamp)`.

**Tabla de mapeos:**

| Excepción | HTTP | Cuándo ocurre |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entidad no encontrada por ID o código |
| `DuplicateResourceException` | 409 | Violación de unicidad a nivel de negocio |
| `DataIntegrityViolationException` | 409 | Violación de constraint en DB (con mensaje específico si hay constraint conocido, ej: `uc_tubo_caja_posicion`) |
| `BusinessRuleException` | 422 | Regla de negocio violada (ej: transición de estado inválida, score fuera de rango) |
| `MethodArgumentNotValidException` | 400 | Falla de validación Jakarta (`@NotBlank`, `@NotNull`, etc.) |
| `BadCredentialsException` | 401 | Credenciales de login incorrectas |
| `DisabledException` | 403 | Login de profesional dado de baja (`activo = false`) |
| `AccessDeniedException` | 403 | `@PreAuthorize` rechazó el rol |
| `Exception` (catch-all) | 500 | Error inesperado (logueado con SLF4J) |

Los controllers no atrpan excepciones manualmente — las lanzan y el handler las procesa.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Try-catch en cada controller** | Duplicación masiva; el formato de error varía según quien escribió el método |
| **`ResponseEntity` con status manual en el Service** | El Service no debería conocer HTTP — mezcla responsabilidades |
| **Dejar el manejo por defecto de Spring** | Devuelve Whitelabel Error Page o JSON con stack trace en producción — inadmisible |

## Consecuencias

**Positivas:**
- El frontend tiene un contrato predecible: siempre `{ status, error, message, timestamp }` para cualquier error
- Los controllers son más limpios: lanzan excepciones de dominio y no piensan en HTTP codes
- Agregar un nuevo tipo de error = agregar un `@ExceptionHandler` en un solo lugar
- Los errores de constraint conocidos se traducen a mensajes de usuario (ej: "La posición seleccionada ya está ocupada en esta caja")

**Negativas / trade-offs:**
- El handler centralizado debe conocer todas las excepciones del sistema — si alguien agrega una excepción nueva y no agrega el handler, cae en el catch-all de 500
- `BusinessRuleException` y `DuplicateResourceException` mapean ambas a status de conflicto (422 y 409 respectivamente) — la distinción semántica debe ser consistente en todo el código
