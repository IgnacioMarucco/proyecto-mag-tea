# ADR-0011: Acceso público al M-CHAT mediante token UUID con expiración

**Estado:** Aceptado

---

## Contexto

El formulario M-CHAT-R/F es completado por la familia del niño desde su casa. El flujo clínico es:

1. El médico admite al paciente y el sistema envía un email con un link único a la familia
2. La familia abre el link y completa el formulario (20 preguntas, ~5 minutos)
3. El sistema calcula el score, lo asocia al paciente y notifica al médico

Este formulario requiere acceso sin login desde el dominio público. Al mismo tiempo, debe estar vinculado a un paciente específico y no debe ser accesible arbitrariamente.

El desafío es: ¿cómo permitir acceso sin autenticación a un recurso clínico específico, de forma segura y con control de tiempo?

---

## Decisión

Se usa un **token UUID con expiración** almacenado en el registro `Paciente`:

- Campos en `Paciente`: `mchatToken` (String UUID) y `mchatTokenExpiry` (LocalDateTime), ambos marcados `@NotAudited` (ver ADR-0031)
- El token se genera con `UUID.randomUUID()` y tiene vigencia configurable (default: 30 días, vía `app.mchat.token-expiry-days`)
- El endpoint público es `/api/v1/public/mchat/{token}` — cubierto por `SecurityConfig` con `.requestMatchers(ApiConstants.V1 + "/public/**").permitAll()`
- La validación verifica: (a) que el token exista en DB, (b) que `mchatTokenExpiry` sea futuro

```java
// MchatTokenService
Paciente p = repository.findByMchatToken(token)
    .filter(pac -> pac.getMchatTokenExpiry() != null
            && pac.getMchatTokenExpiry().isAfter(LocalDateTime.now(clock)))
    .orElseThrow(() -> new ResourceNotFoundException("El enlace no es válido o ha expirado"));
```

- El médico puede regenerar el token en cualquier momento (`POST /pacientes/{codigo}/reenviar-mchat`)
- El frontend Angular accede a la ruta `/mchat/:token` sin `authGuard` — está fuera del módulo `internal`

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Link con parámetro `?pacienteId=123`** | Expone el ID interno; cualquier persona puede acceder al formulario de cualquier paciente adivinando IDs |
| **Cuenta temporal para la familia** | Agrega registro/login al flujo de la familia — demasiada fricción para un formulario de 5 minutos |
| **JWT de corta duración** | El email puede tardar en llegar o la familia puede completarlo días después; un JWT de horas caduca demasiado pronto. El token en DB permite expiración configurable y revocación explícita |
| **OTP por SMS** | Agrega dependencia de SMS y complejidad de verificación en dos pasos innecesaria |

## Consecuencias

**Positivas:**
- El acceso es seguro: sin UUID válido y vigente no hay respuesta útil
- El token es revocable: regenerar uno nuevo invalida el anterior automáticamente
- El flujo para la familia es un solo link — sin cuenta, sin contraseña
- La expiración es configurable por entorno

**Negativas / trade-offs:**
- Un link reenviado manualmente entre conocidos permite que otra persona complete el formulario — aceptable dado el contexto clínico donde se asume cooperación familiar
- Los campos `mchatToken` y `mchatTokenExpiry` añaden estado operacional al registro clínico de `Paciente` — son campos de infraestructura viviendo en una entidad de dominio
