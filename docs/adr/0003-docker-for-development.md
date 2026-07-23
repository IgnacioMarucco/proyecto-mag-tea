# ADR-0003: Docker como entorno de desarrollo

## Contexto

El proyecto tiene tres servicios (frontend Angular, backend Spring Boot, base de datos PostgreSQL) que deben funcionar juntos en desarrollo, en evaluación, y en producción. Las diferencias de entorno entre máquinas son una fuente frecuente de problemas.

## Decisión

Se usa **Docker Compose** con dos archivos: `docker-compose.yml` (base, también sirve para producción) y `docker-compose.dev.yml` (override de desarrollo con hot reload y volúmenes montados).

## Alternativas consideradas

| Alternativa | Por qué no |
|-------------|-----------|
| **Instalación local** | "Funciona en mi máquina" — diferencias de versión y SO generan problemas difíciles de reproducir |
| **Máquina virtual** | Lenta, consume muchos recursos, difícil de compartir |

## Consecuencias

**Positivas:**
- Entorno reproducible: cualquier máquina levanta el sistema con `docker compose up`
- Aislamiento: la base de datos de desarrollo no interfiere con otras instalaciones
- Hot reload en desarrollo: cambios visibles sin reconstruir imagen
- El tribunal puede evaluar sin instalar dependencias

**Negativas / trade-offs:**
- Requiere Docker instalado en la máquina del evaluador
- El rebuild de imagen tras cambios en `pom.xml` o `package.json` agrega tiempo
