# ADR-0009: Arquitectura de subdominios para separar portal público e interno

**Estado:** Aceptado

---

## Contexto

El sistema MAG-TEA tiene dos audiencias completamente distintas:

1. **Familias del público general** — acceden a la landing, al formulario de interés y (opcionalmente) al formulario M-CHAT enviado por mail. No tienen cuenta en el sistema.
2. **Profesionales internos** — médicos e investigadores que gestionan el protocolo completo. Requieren login y rol.

Servir ambas audiencias desde el mismo origen (`/`) con rutas separadas funciona, pero genera ruido en la navegación y en los guards: cada guard necesita saber si el usuario "llegó por la ruta pública" o "llegó por la ruta interna".

---

## Decisión

Se usan **dos subdominios** como punto de entrada, manejados con guards en el router de Angular:

| Entorno | URL pública | URL interna |
|---|---|---|
| Desarrollo | `http://localhost:4200` | `http://app.localhost:4200` |
| Producción | `https://magtea.org` | `https://app.magtea.org` |

**`publicSubdomainGuard`** — aplicado a la ruta `/`: si el hostname comienza con `app.`, redirige a `/login`.

**`appSubdomainGuard`** — aplicado a `/internal`: si el hostname **no** comienza con `app.`, redirige a `/`.

```typescript
const isAppSubdomain = () => window.location.hostname.startsWith('app.');

export const appSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return isAppSubdomain() || router.createUrlTree(['/']);
};

export const publicSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return !isAppSubdomain() || router.createUrlTree(['/login']);
};
```

Las rutas hijas de `/internal` no necesitan guard propio — heredan el del padre automáticamente.

`app.localhost` es resuelto nativamente por los navegadores modernos sin configuración de hosts adicional.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **Rutas separadas en el mismo origen** (`/public`, `/app`) | Los guards dependen del path, no del subdominio — más difícil de razonar; la URL no comunica a qué sistema pertenece |
| **Dos aplicaciones Angular independientes** | Duplica la build, el pipeline de despliegue y los componentes compartidos |
| **Path prefix `/internal` sin subdominio** | Funciona pero el usuario puede navegar manualmente a rutas internas desde el dominio público — hay que blindar todo con guards de path |

## Consecuencias

**Positivas:**
- El subdominio es la primera línea de separación: un usuario del público no puede "navegar" accidentalmente al portal interno
- Los guards son simples — una sola función `isAppSubdomain()` basada en `hostname`
- El link "Acceso Investigadores" en el footer construye la URL del subdominio dinámicamente desde `window.location`, funcionando igual en dev y prod sin configuración
- Nginx en producción puede servir la misma SPA para ambos subdominios con una sola config

**Negativas / trade-offs:**
- Requiere que el servidor (Nginx/proxy) reconozca ambos subdominios en producción
- Los links absolutos entre subdominio público e interno deben construirse dinámicamente (no pueden ser relativos)
