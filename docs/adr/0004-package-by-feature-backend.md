# ADR-0004: Organización de paquetes por feature en el backend

## Contexto

El backend tiene múltiples entidades del dominio (Profesional, Paciente, Suero, Pool, ModeloAnimal). Hay dos formas estándar de organizar los paquetes Java: por capa técnica o por feature de dominio.

## Decisión

Se usa **package by feature**: cada entidad del dominio vive en su propio paquete con todos sus artefactos (Entity, Repository, Service, Controller, DTO, Mapper).

```
com.utn.magtea/
├── profesional/   → Profesional + Repository + Service + Controller + DTO + Mapper
├── paciente/
└── suero/
```

## Alternativas consideradas

**Package by layer** — todas las clases del mismo tipo agrupadas (`controllers/`, `services/`, `repositories/`). Descartado porque para entender una sola entidad hay que navegar entre 4 o 5 carpetas distintas.

## Consecuencias

**Positivas:**
- Alta cohesión: todo lo de una entidad está en un lugar
- Bajo acoplamiento: modificar Suero no requiere tocar Paciente
- Agregar una entidad nueva = agregar una carpeta sin modificar las existentes
- Alineado con DDD: la estructura refleja el lenguaje del dominio médico

**Negativas / trade-offs:**
- Clases técnicas similares (ej: todos los Controllers) quedan dispersas — requiere buscar por entidad, no por tipo
