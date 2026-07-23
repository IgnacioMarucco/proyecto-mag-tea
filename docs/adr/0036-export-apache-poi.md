# ADR-0036: Exportación de datos con Apache POI (CSV y XLSX multi-hoja)

**Estado:** Aceptado

---

## Contexto

La investigadora principal necesita exportar los datos del sistema para análisis estadístico externo (SPSS, R, Excel). Los datos relevantes son tres conjuntos:

1. **Datos clínicos de pacientes** — variables demográficas, scores M-CHAT, CARS, Vineland
2. **Datos de modelos animales** — parámetros conductuales (VUS, tres cámaras) y microscopía
3. **Composición de pools** — qué sueros componen cada pool y sus rangos BTU

El volumen esperado es ~500 pacientes y ~200 modelos animales — manejable en memoria.

---

## Decisión

Se usa **Apache POI 5.x** en el backend para generar archivos XLSX multi-hoja. Se agrega soporte CSV para el formato más simple.

**`ExportController`** bajo `GET /api/v1/exportar`, restringido a `INVESTIGADOR_PRINCIPAL`:

| Endpoint | Formato | Contenido |
|---|---|---|
| `GET /ratones` | CSV | Datos básicos de modelos animales + estudios conductuales |
| `GET /ratones/xlsx` | XLSX | Ídem, en Excel |
| `GET /pacientes` | CSV | Variables clínicas de pacientes |
| `GET /pacientes/xlsx` | XLSX | Ídem, en Excel |
| `GET /pool-composicion` | CSV | Mapping pool → sueros → pacientes |
| `GET /pool-composicion/xlsx` | XLSX | Ídem, en Excel |
| `GET /completo/xlsx` | XLSX multi-hoja | Las tres secciones anteriores en un solo archivo con tres hojas |

La generación es **server-side, síncrona, en memoria** — el archivo se construye con `XSSFWorkbook` y se envía como `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` con `Content-Disposition: attachment`.

El frontend (`exportacion.component`) ofrece botones de descarga por tipo y formato, consumiendo los endpoints con `responseType: 'blob'`.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Exportación client-side (JavaScript)** | El frontend no tiene acceso directo a la DB; requeriría cargar todos los datos primero vía API |
| **JasperReports** | Orientado a reportes visuales (PDF); agrega XML de diseño de reporte que es innecesario para dumps de datos |
| **OpenCSV solo** | No soporta XLSX; los investigadores prefieren Excel para análisis directo |
| **Streaming / chunked transfer** | El volumen (~700 filas) no justifica la complejidad de streaming |

## Consecuencias

**Positivas:**
- La investigadora descarga un solo archivo XLSX con las tres hojas y lo abre directamente en Excel/R
- El formato está libre de la paginación — la exportación siempre trae todos los registros activos
- El endpoint CSV satisface herramientas como R o Python que prefieren CSV sobre XLSX

**Negativas / trade-offs:**
- Generación en memoria: con volúmenes muy grandes (>50.000 filas) podría ser un problema — no relevante para el proyecto
- El formato de las columnas está hardcodeado en el Service — si cambia el schema, hay que actualizar la exportación manualmente
- Apache POI es una dependencia de ~10 MB que impacta el tamaño del JAR
