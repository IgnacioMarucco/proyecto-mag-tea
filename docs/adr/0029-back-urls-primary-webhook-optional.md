# ADR-0029: back_urls como mecanismo primario de redirect + webhook como opcional

## Contexto

Tras completar el pago en Mercado Pago, el sistema necesita:
1. Mostrarle al usuario un mensaje de resultado (aprobado / rechazado / pendiente)
2. Actualizar el estado de la `Donacion` en la base de datos

MP ofrece dos mecanismos para esto:

- **back_urls**: MP redirige el navegador del usuario a la URL configurada, añadiendo `?status=approved|failure|pending` como query param. Funciona con cualquier URL, incluso `localhost`.
- **Webhook (IPN)**: MP hace un `POST` server-to-server a `notificationUrl` con el ID del pago. Requiere que el backend sea accesible desde internet (URL pública bajo HTTPS).

## Decisión

Se usa **back_urls como mecanismo primario** para la experiencia del usuario. El webhook es **opcional** y se activa solo si `MP_NOTIFICATION_URL` está configurado en el entorno.

- `back_urls.success/failure/pending` → todas apuntan a `{MP_BACK_URL_BASE}/donacion/resultado`
- El frontend lee el query param `?status=` y muestra el mensaje correspondiente
- El webhook, si llega, actualiza el estado real de la Donacion en DB consultando la Payment API

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Solo webhook, sin back_urls** | El usuario no recibe feedback visual inmediato; el webhook no es testeable en `localhost` sin ngrok |
| **back_urls obligatorio + webhook obligatorio** | Hacer el webhook obligatorio requiere ngrok o un servidor público en desarrollo, añadiendo fricción innecesaria al setup local |
| **Polling desde el frontend** | Requiere endpoint de estado, agrega carga al backend y complejidad al frontend |

## Consecuencias

**Positivas:**
- El flujo de UX funciona en `localhost` sin configuración adicional
- El webhook es una mejora incremental: sin él el sistema funciona, con él el estado de DB es más preciso
- `MP_NOTIFICATION_URL` vacío = sin webhook; no hay errores ni configuración extra en desarrollo

**Negativas / trade-offs:**
- Sin webhook, la Donacion queda en estado `PENDIENTE` en DB incluso cuando el pago fue aprobado — el estado en DB no es confiable hasta que el webhook esté configurado
- `autoReturn` (redirect automático sin click del usuario) solo funciona con URLs públicas; en `localhost` el usuario debe hacer clic en "Volver al sitio"
- En producción se debe configurar `MP_NOTIFICATION_URL` para tener estados actualizados automáticamente
