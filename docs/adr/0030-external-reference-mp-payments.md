# ADR-0030: externalReference de MP para vincular pagos con registros internos

## Contexto

Al procesar el webhook de Mercado Pago, el backend recibe un `paymentId` de MP y necesita encontrar la `Donacion` correspondiente en la base de datos para actualizar su estado.

Existen dos formas de hacer esta vinculación:

1. **`preferenceId`**: al crear la preferencia MP devuelve un ID de preferencia. Guardarlo en `Donacion.mpPreferenceId` y luego buscar por ese campo cuando llega el webhook.
2. **`externalReference`**: campo libre que se envía al crear la preferencia en MP. MP lo devuelve en el objeto `Payment` al consultar la Payment API. Se puede usar el ID interno de la `Donacion`.

Durante el desarrollo se descubrió que el SDK Java 2.1.29 no expone `payment.getPreferenceId()` — el método no existe en esa versión. La alternativa de buscar la `Donacion` por `mpPreferenceId` requeriría llamar a la Preference API para obtener el preferenceId a partir del paymentId, añadiendo una llamada HTTP extra.

## Decisión

Se usa **`externalReference`** como mecanismo de vinculación:

1. La `Donacion` se guarda en DB primero para obtener su `id`
2. Al crear la preferencia en MP, se setea `externalReference = donacion.getId().toString()`
3. El webhook llama a la Payment API: `paymentClient.get(paymentId)`
4. Se lee `payment.getExternalReference()` para encontrar la `Donacion` por ID

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Buscar por `mpPreferenceId`** | `payment.getPreferenceId()` no existe en SDK 2.1.29; requeriría llamar a la Preference API como paso intermedio |
| **Guardar el paymentId al crear la preferencia** | El paymentId no está disponible al momento de crear la preferencia — solo se conoce después del pago |
| **Tabla de correlación separada** | Añade una entidad y una join innecesarios para un caso simple |

## Consecuencias

**Positivas:**
- Una sola llamada a la Payment API resuelve tanto el estado como la vinculación con el registro interno
- Compatible con el SDK Java 2.1.29 sin necesidad de llamadas adicionales
- El campo `mpPreferenceId` se sigue guardando en `Donacion` (disponible para auditoría y debug), pero no se usa como clave de búsqueda

**Negativas / trade-offs:**
- La `Donacion` debe guardarse en DB **antes** de crear la preferencia en MP para obtener el ID. Si MP falla, queda un registro en estado `PENDIENTE` con `mpPreferenceId = null` — es un "draft" inofensivo pero puede generar ruido en reportes
- `externalReference` es un campo libre de texto en MP — no hay validación de tipo; se parsea con `Long.parseLong()`, que lanza excepción si el valor no es numérico (protegido por try-catch en el webhook handler)
