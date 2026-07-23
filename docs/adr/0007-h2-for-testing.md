# ADR-0007: H2 en memoria como base de datos de tests

## Contexto

Los tests de integración del backend (ControllerTest con MockMvc) necesitan una base de datos. Se necesita una opción que sea rápida, no requiera servicios externos y aísle los tests entre sí.

## Decisión

Se usa **H2 en memoria** con perfil `test`. Hibernate genera el schema con `ddl-auto: create-drop` al inicio de cada test. Cada test revierte sus cambios con `@Transactional`.

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **PostgreSQL real** | Requiere que la DB esté corriendo, los tests pueden contaminar datos reales |
| **Testcontainers** | Requiere Docker en CI, más lento, mayor complejidad de configuración |

## Consecuencias

**Positivas:**
- Tests corren en segundos sin servicios externos
- Cada test empieza con DB limpia y vacía
- Corren en cualquier entorno con solo Java instalado (developer, CI, máquina del tutor)

**Negativas / trade-offs:**
- H2 no es 100% compatible con PostgreSQL — algunas funcionalidades específicas de Postgres (JSON avanzado, funciones propias) no se pueden testear
- Este trade-off es aceptable para este proyecto ya que no se usan esas funcionalidades
