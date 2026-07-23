# ADR-0005: Convención de idioma del código

**Estado:** Aceptado

---

## Contexto

El proyecto MAG-TEA involucra dos mundos: el dominio clínico-científico del Centro Wernicke / CIQUIBIC (con vocabulario en español) y la infraestructura técnica de software (donde la nomenclatura estándar es inglés). Toda la comunicación con el equipo médico —reuniones, documentación de protocolo, nombres de entidades— ocurre en español.

Mezclar ambos idiomas sin regla explícita genera código inconsistente: `PatientRepository`, `findSuero()`, `getActivo()` conviven sin criterio.

---

## Decisión

Se usa **español para conceptos de dominio** e **inglés para infraestructura/técnica**:

| Categoría | Idioma | Ejemplos |
|---|---|---|
| Entidades de dominio y sus paquetes | Español | `Paciente`, `Suero`, `Pool`, `ModeloAnimal`, `formulario`, `activo` |
| Campos de dominio clínico | Español | `fechaNacimientoNino`, `nombreNino`, `valorAnticuerpos`, `estadoClinico` |
| Métodos y variables de infraestructura | Inglés | `findById`, `isActive`, `createdAt`, `request`, `result`, `service` |
| Campos técnicos transversales | Inglés | `createdAt`, `updatedAt`, `createdBy`, `lastModifiedBy` |
| Mensajes de validación y comentarios | Español | `"El campo nombre es obligatorio"` |
| Labels de UI | Español | `"Guardar cambios"`, `"Cancelar"` |

**Regla práctica:** si es un concepto que el médico o investigador usaría al describir el sistema → español. Si es infraestructura/técnico → inglés.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Todo inglés** | `Patient`, `Serum`, `AnimalModel` — el vocabulario médico en español no traduce bien; el equipo clínico no reconoce las entidades |
| **Todo español** | `encontrarPorId()`, `guardar()` — colisiona con convenciones de frameworks (Spring, Angular) y rompe integración con anotaciones y herramientas |

## Consecuencias

**Positivas:**
- El código refleja el lenguaje ubicuo del dominio — una entidad `Paciente` es inmediatamente reconocible para el equipo clínico
- La separación es clara y mecánica: los colaboradores nuevos aprenden la regla en un ejemplo

**Negativas / trade-offs:**
- Algunos campos mezclan ambos idiomas en un nombre compuesto: `mchatToken`, `estadoClinico` — aceptable porque la primera parte es acrónimo técnico / la segunda es dominio
