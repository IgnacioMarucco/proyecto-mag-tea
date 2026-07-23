# ADR-0008: JWT para autenticación

## Contexto

El sistema tiene usuarios internos con roles fijos. Se necesita un mecanismo de autenticación que no requiera estado en el servidor y que permita al frontend leer el rol del usuario sin requests adicionales.

## Decisión

Se usa **JWT (JSON Web Token)** firmado con Spring Security. El token se envía en el header `Authorization: Bearer <token>` en cada request. El rol está embebido en el payload del token.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Sesiones con cookie** | Requiere almacenamiento de sesión en servidor (memoria o Redis), no escala horizontalmente sin sticky sessions |
| **OAuth2 / OpenID Connect** | Agrega dependencia externa innecesaria para un sistema con usuarios internos y roles fijos |

## Consecuencias

**Positivas:**
- Stateless: el servidor no almacena sesiones, cada request es independiente
- Rol embebido: el frontend lee el rol del JWT para filtrar navegación sin request adicional
- Escalable: múltiples instancias del servidor verifican el mismo token sin compartir estado

**Negativas / trade-offs:**
- Tokens no se pueden invalidar antes de su expiración sin lista negra adicional (no implementada)
- Si el secreto de firma se compromete, todos los tokens emitidos quedan vulnerables
