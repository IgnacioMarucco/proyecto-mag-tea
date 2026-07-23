# ADR-0020: El servidor asigna la fechaContacto del formulario público

## Contexto

El formulario de interés que completan las familias desde el portal público no debe incluir un campo `fechaContacto` — las familias no tienen contexto de qué fecha poner y podrían manipularla.

## Decisión

El Service asigna `LocalDate.now()` al crear el formulario, ignorando cualquier valor que pudiera venir del cliente en el request body.

## Alternativas consideradas

**El cliente envía la fecha** — descartado porque cualquier usuario podría manipular la fecha enviando un valor arbitrario en el body.

## Consecuencias

**Positivas:**
- Integridad del dato: la fecha refleja cuándo llegó el formulario al sistema, no cuándo lo dice el cliente
- Formulario público más simple para las familias
- Todos los formularios tienen fecha confiable y comparable

**Negativas / trade-offs:**
- La fecha depende de la zona horaria del servidor — documentada como `America/Argentina/Cordoba` en `application.yaml`
