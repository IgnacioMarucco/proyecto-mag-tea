# ADR-0006: MapStruct para mapeo entre entidades y DTOs

## Contexto

El backend separa entidades JPA de DTOs de request/response. El mapeo entre ambos es repetitivo y propenso a errores si se hace manualmente.

## Decisión

Se usa **MapStruct** con `@Mapper(componentModel = "spring")`. El código de mapeo se genera en tiempo de compilación a partir de interfaces declarativas.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Mapeo manual** | Verboso y propenso a olvidar campos al agregar atributos nuevos a la entidad |
| **ModelMapper** | Mapeo por reflexión en runtime: lento, errores detectados en runtime no en compilación |

## Consecuencias

**Positivas:**
- Errores de mapeo detectados en compilación (campo sin mapear, tipos incompatibles)
- Rendimiento: código generado es Java puro, sin reflexión
- Cero boilerplate: se declara la interfaz, MapStruct genera la implementación
- Bean Spring inyectable directamente con `componentModel = "spring"`

**Negativas / trade-offs:**
- Configuración extra en `pom.xml` (annotation processor)
- Campos calculados en ResponseDTO requieren `@Mapping(expression = "java(...)")` — menos intuitivo
