# ADR-0010: Autorización en dos capas — sidebar + @PreAuthorize

**Estado:** Aceptado

---

## Contexto

El sistema tiene tres roles internos con acceso diferenciado:

| Rol | Módulos habilitados |
|---|---|
| `CUERPO_MEDICO` | Pacientes, Bandeja de formularios |
| `CUERPO_TECNICO` | Sueros, Pools, Modelos Animales, Cajas, Camadas, Tubos |
| `INVESTIGADOR_PRINCIPAL` | Todos los módulos + Reportes + Exportación |

El rol viene embebido en el JWT (ver ADR-0008). `AuthService.currentUser` es un `signal<CurrentUser | null>` con `{ email, role }` decodificado del token.

Se necesita un mecanismo que: (a) evite que el usuario vea opciones de navegación que no le corresponden, y (b) impida que un usuario con token manipulado acceda a endpoints que no le corresponden.

---

## Decisión

Se usan **dos capas complementarias** sin `roleGuard` en las rutas:

### Capa 1 — UX (sidebar)

`internal-layout.component.ts` define los ítems de navegación con `allowedRoles: Role[]`. Al renderizar el menú, filtra los ítems según el rol del usuario actual:

```typescript
items: section.items.filter(item => item.allowedRoles.includes(role))
```

Esta capa es **solo UX** — evita que el usuario vea opciones irrelevantes, pero no es una barrera de seguridad.

### Capa 2 — Seguridad real (backend)

Cada controller anota la clase o método con `@PreAuthorize`:

```java
@PreAuthorize("hasAnyRole('CUERPO_TECNICO', 'INVESTIGADOR_PRINCIPAL')")
public class SueroController { ... }

@PreAuthorize("hasRole('INVESTIGADOR_PRINCIPAL')")
public class ExportController { ... }
```

Si un usuario sin el rol adecuado llega al endpoint (sea por bug de frontend, manipulación del token o petición directa), Spring Security devuelve 403. El `GlobalExceptionHandler` lo mapea a una respuesta de error estándar.

### Por qué no hay `roleGuard` en las rutas

Un `roleGuard` en Angular protege la navegación en el cliente, pero el cliente no es confiable. El backend con `@PreAuthorize` es la única barrera real. Agregar un `roleGuard` sería redundante con el sidebar (ambas son capas UX) y crearía una falsa sensación de seguridad si el backend no está protegido.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| **`roleGuard` en rutas + `@PreAuthorize`** | El guard de ruta agrega complejidad sin aumentar la seguridad real — el sidebar ya hace la misma función de UX |
| **Solo `@PreAuthorize` sin sidebar** | El usuario puede navegar a rutas sin permiso y ver un error 403 — mala UX |
| **Solo sidebar sin `@PreAuthorize`** | Cualquier petición directa a la API elude la restricción — inseguro |

## Consecuencias

**Positivas:**
- La responsabilidad de cada capa es clara: sidebar = UX, `@PreAuthorize` = seguridad
- Agregar una ruta nueva en `app.routes.ts` solo necesita `canActivate: [authGuard]` (autenticación). La autorización por rol la resuelve el backend
- Un cambio en los roles no requiere tocar el router — solo el sidebar y el controller correspondiente

**Negativas / trade-offs:**
- Un usuario puede teclear manualmente una URL de módulo sin permiso y llegar a un componente que falle al cargar datos (el backend devuelve 403). El componente debe manejar ese error gracefully
