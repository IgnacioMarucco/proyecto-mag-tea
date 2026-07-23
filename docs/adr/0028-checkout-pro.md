# ADR-0028: Mercado Pago Checkout Pro (redirect) en lugar de Checkout Bricks (embebido)

## Contexto

El módulo de donaciones requiere procesar pagos con Mercado Pago. MP ofrece dos opciones principales:

- **Checkout Pro**: el backend crea una "preferencia" vía API y devuelve un `initPoint` (URL). El frontend redirige al usuario a esa URL. El flujo de pago ocurre completamente en la página de MP.
- **Checkout Bricks**: formulario de pago embebido en la propia página mediante un SDK JavaScript. El usuario no sale del sitio.

## Decisión

Se usa **Checkout Pro** con creación de preferencia server-side y redirect desde el frontend al `initPoint` devuelto por el backend.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Checkout Bricks** | Requiere integrar el SDK JS de MP en Angular, gestionar el ciclo de vida del widget y manejar la tokenización de la tarjeta en el frontend. Complejidad innecesaria para el volumen esperado de donaciones del proyecto |
| **Link de pago estático en el panel de MP** | No permite registrar la donación en la DB ni asociar datos del donante antes del pago |
| **Botón de pago de MP** | Similar al link estático: sin control sobre el flujo ni trazabilidad interna |

## Consecuencias

**Positivas:**
- El backend controla todo el flujo: guarda la Donacion, crea la preferencia y registra el `mpPreferenceId`
- MP se encarga de la seguridad del formulario de pago (PCI compliance)
- Sin dependencia de SDK JavaScript en el frontend
- Fácil de testear con tarjetas de prueba del sandbox

**Negativas / trade-offs:**
- El usuario sale del dominio de la app para pagar; sin `autoReturn`, debe hacer clic en "Volver al sitio" para ver el resultado
- `autoReturn` (redirect automático post-pago) solo funciona con URLs públicas bajo HTTPS — no disponible en `localhost` durante desarrollo
