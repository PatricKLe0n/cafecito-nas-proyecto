# Diseño — CRUD de Trazabilidad de Café de Especialidad

- **Fecha:** 2026-06-16
- **Autor:** Equipo de desarrollo (prueba técnica)
- **Estado:** Aprobado — listo para plan de implementación

## 1. Contexto y objetivo

Prueba técnica: implementar un **mantenimiento (CRUD)** sobre un proceso real de una
industria a elección, valorando **originalidad** y **mejoras** (cloud, virtualización,
Spring Security, etc.).

Se ha elegido el dominio de **café de especialidad**, modelando la **trazabilidad
"de la finca a la taza"**: el recorrido de un lote de café desde su origen en una finca,
pasando por su recepción como café verde, hasta su transformación en un lote tostado.
Es un dominio original, con relaciones jerárquicas claras, reglas de negocio reales
(control de stock, mermas de tueste, máquina de estados) y autorización por rol.

### Stack acordado

| Capa | Tecnología |
|---|---|
| Frontend | Angular 18 (standalone components) + TypeScript + **Tailwind CSS** + componentes propios |
| Backend | Java 21 + Spring Boot 3.3 (Maven) |
| Arquitectura backend | Por capas limpia: Controller → Service → Repository |
| Seguridad | Spring Security + **JWT** (HS256, BCrypt), roles ADMIN/USER |
| Persistencia | Spring Data JPA + **Flyway** (migraciones versionadas) |
| Base de datos | PostgreSQL 16 |
| Documentación API | springdoc-openapi (Swagger UI) |
| Mapeo DTO | MapStruct |
| Tests | JUnit 5 + Mockito (lógica de negocio del tostado) |

### Mejoras incluidas (sobre el CRUD base)

- **Spring Security + JWT** con autorización por rol (ADMIN escribe, USER solo lee).
- **Swagger/OpenAPI** para documentación interactiva de la API.
- **Flyway** para versionado del esquema (buena práctica de datos).
- Diseño de UI **distintivo con Tailwind** (identidad visual de tostaduría), no plantilla genérica.

### Fuera de alcance (no elegido)

- Docker / docker-compose.
- CI/CD y despliegue en cloud.
- Tests end-to-end de frontend (solo tests unitarios de negocio en backend).

## 2. Modelo de dominio

Flujo de trazabilidad encadenado:

```
Finca (origen)  1 ──< n  LoteCafeVerde  1 ──< n  LoteTostado  (CRUD principal)

Usuario  n >── 1  Rol   (Spring Security + JWT)
```

### 2.1. Finca (maestro de origen)

| Campo | Tipo | Reglas |
|---|---|---|
| id | Long (PK) | autogenerado |
| pais | String | obligatorio |
| region | String | obligatorio |
| nombre | String | obligatorio, único |
| productor | String | opcional |
| altitudMsnm | Integer | 0–4000 |
| variedad | String | p. ej. Caturra, Geisha, Bourbon |
| proceso | Enum {LAVADO, NATURAL, HONEY} | obligatorio |

Regla: no se puede eliminar una finca con lotes de café verde asociados (409).

### 2.2. LoteCafeVerde (café crudo recibido)

| Campo | Tipo | Reglas |
|---|---|---|
| id | Long (PK) | autogenerado |
| codigo | String | obligatorio, único |
| finca | FK → Finca | obligatorio |
| pesoKg | BigDecimal | > 0 |
| humedadPorcentaje | BigDecimal | 0–100 |
| puntajeSca | BigDecimal | 0–100 (cupping score) |
| fechaRecepcion | LocalDate | no futura |
| estado | Enum {DISPONIBLE, AGOTADO} | derivado del stock |

Regla: no se puede eliminar un lote verde con lotes tostados asociados (409).
El `pesoKg` representa el stock disponible; se descuenta al tostar.

### 2.3. LoteTostado (CRUD principal)

| Campo | Tipo | Reglas |
|---|---|---|
| id | Long (PK) | autogenerado |
| codigo | String | obligatorio, único |
| loteCafeVerde | FK → LoteCafeVerde | obligatorio |
| perfilTueste | Enum {LIGHT, MEDIUM, DARK} | obligatorio |
| pesoEntradaKg | BigDecimal | > 0, ≤ stock disponible del lote verde |
| pesoSalidaKg | BigDecimal | > 0 y < pesoEntradaKg |
| mermaPorcentaje | BigDecimal | **derivado** = (entrada − salida) / entrada × 100 |
| fechaTueste | LocalDateTime | no futura |
| estado | Enum {REGISTRADO, ANULADO} | |

Reglas de negocio (en `LoteTostadoService`, transaccional):

1. `pesoSalidaKg < pesoEntradaKg` (si no → 409 regla de negocio).
2. `pesoEntradaKg ≤ stock disponible` del lote verde (si no → 409).
3. `mermaPorcentaje` se calcula en el servidor (nunca se acepta del cliente).
4. Al crear: se descuenta `pesoEntradaKg` del `pesoKg` del lote verde;
   si el stock resultante es 0 → el lote verde pasa a `AGOTADO`.
5. Al anular (DELETE lógico → estado ANULADO): se devuelve el peso al lote verde
   y se recalcula su estado.
6. **Edición (PUT):** solo se permiten cambios en campos que no afectan al stock
   (`codigo`, `perfilTueste`, `fechaTueste`). Los pesos (`pesoEntradaKg`,
   `pesoSalidaKg`) no son editables tras el alta; para corregirlos se anula el
   lote y se crea uno nuevo. Así se evita la re-conciliación compleja de stock.

### 2.4. Usuario y Rol (seguridad)

| Campo | Tipo | Reglas |
|---|---|---|
| id | Long (PK) | autogenerado |
| username | String | obligatorio, único |
| password | String | BCrypt, nunca se devuelve |
| rol | Enum {ADMIN, USER} | obligatorio |

- **ADMIN:** CRUD completo sobre las tres entidades.
- **USER:** solo lectura (GET).

Usuarios semilla (Flyway/seed): `admin/admin123` (ADMIN), `user/user123` (USER).
Contraseñas semilla solo para la demo de la prueba.

## 3. Arquitectura del backend

Arquitectura **por capas limpia**, un paquete por feature:

```
com.cafe.trazabilidad
├── config/            (SecurityConfig, OpenApiConfig, CORS)
├── security/          (JwtService, JwtAuthFilter, UserDetails, AuthController)
├── common/            (GlobalExceptionHandler, ApiError, PageResponse, BaseEntity)
├── finca/             (Controller, Service, Repository, entity, dto, mapper)
├── loteverde/         (idem)
└── lotetostado/       (idem)
```

Por cada feature:

- **Controller** — expone REST, recibe/devuelve **DTOs**, valida con `@Valid`,
  no contiene lógica de negocio.
- **Service** — lógica de negocio y transacciones (`@Transactional`).
- **Repository** — `JpaRepository` + `JpaSpecificationExecutor` para filtros.
- **Mapper** — MapStruct entre entidad y DTO (request/response separados).

### 3.1. API REST

Convenciones: prefijo `/api`, paginación Spring (`page`, `size`, `sort`),
parámetro `q` para búsqueda por texto y filtros específicos por entidad.

```
POST   /api/auth/login                          público   → { token, username, rol, expiraEn }

GET    /api/fincas?page&size&sort&q             USER+
GET    /api/fincas/{id}                         USER+
POST   /api/fincas                              ADMIN
PUT    /api/fincas/{id}                          ADMIN
DELETE /api/fincas/{id}                          ADMIN

GET    /api/lotes-verdes?page&size&sort&q&fincaId&estado   USER+
GET    /api/lotes-verdes/{id}                    USER+
POST   /api/lotes-verdes                         ADMIN
PUT    /api/lotes-verdes/{id}                     ADMIN
DELETE /api/lotes-verdes/{id}                     ADMIN

GET    /api/lotes-tostados?page&size&sort&q&perfil&estado&desde&hasta   USER+
GET    /api/lotes-tostados/{id}                  USER+
POST   /api/lotes-tostados                       ADMIN
PUT    /api/lotes-tostados/{id}                   ADMIN
DELETE /api/lotes-tostados/{id}  (anulación)      ADMIN

GET    /api/dashboard/resumen                    USER+  (totales para el panel)
```

### 3.2. Respuesta paginada estándar

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### 3.3. Manejo de errores

`@RestControllerAdvice` global con formato consistente:

```json
{
  "timestamp": "2026-06-16T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Errores de validación",
  "path": "/api/lotes-tostados",
  "fieldErrors": [ { "field": "pesoSalidaKg", "message": "debe ser menor que el peso de entrada" } ]
}
```

Mapeo de excepciones:

| Situación | HTTP |
|---|---|
| Validación de entrada (Bean Validation) | 400 |
| Recurso no encontrado | 404 |
| Violación de regla de negocio (merma, stock, borrado con dependencias) | 409 |
| No autenticado / token inválido | 401 |
| Autenticado sin permiso (USER intenta escribir) | 403 |

### 3.4. Seguridad (JWT)

- `SecurityFilterChain` stateless; `JwtAuthFilter` valida el token en cada request.
- `JwtService` firma/verifica HS256 con secreto e issuer configurables y expiración.
- Autorización por método/ruta con `@PreAuthorize` o reglas en la cadena de filtros.
- `PasswordEncoder` = BCrypt.
- CORS habilitado para el origen del frontend Angular (configurable).

## 4. Arquitectura del frontend (Angular + Tailwind)

```
src/app
├── core/         (auth.service, token.interceptor, auth.guard, api base, models)
├── shared/       (tabla-datos, paginador, modal-confirmacion, badge-estado, input/select propios)
├── features/
│   ├── auth/         (login)
│   ├── dashboard/    (resumen con totales)
│   ├── fincas/       (lista + formulario)
│   ├── lotes-verdes/ (lista + formulario)
│   └── lotes-tostados/ (lista + formulario, CRUD principal)
└── layout/       (shell con barra lateral / topbar)
```

- **Standalone components** + lazy loading por feature.
- **Reactive Forms** con validación que refleja las reglas del backend.
- **HttpClient** con **interceptor** que añade `Authorization: Bearer <token>`
  y maneja 401 (redirige a login).
- **Guard** de ruta que exige sesión; las acciones de escritura (crear/editar/borrar)
  se ocultan/deshabilitan para rol USER.
- **Tabla reutilizable** con paginación, ordenación y búsqueda servidor.
- **Modal/panel propio** para alta/edición y diálogo de confirmación de borrado.

### 4.1. Dirección visual

Identidad de tostaduría, intencional y no genérica:

- Paleta: espresso `#3B2417`, crema `#F5EFE6`, ámbar de acento `#C8893F`,
  verdes/marrones de apoyo; modo claro.
- Tipografía con contraste editorial (display serif para títulos, sans legible para datos).
- Badges por color para `estado` y para `perfilTueste` (Light/Medium/Dark).
- Dashboard ligero: lotes verdes disponibles, lotes tostados del mes, merma media,
  kg tostados.

## 5. Persistencia y migraciones

- **Flyway** con scripts versionados en `backend/src/main/resources/db/migration`:
  - `V1__schema.sql` — tablas, claves foráneas, índices, constraints.
  - `V2__seed.sql` — usuarios semilla + datos de ejemplo (fincas, lotes) para la demo.
- Tipos: `BigDecimal`/`NUMERIC` para pesos y porcentajes; enums como `VARCHAR` con check.
- Índices en claves foráneas y en campos de búsqueda (`codigo`, `nombre`).

## 6. Estrategia de pruebas

Baseline profesional, centrado en el valor:

- **Unitarias (JUnit 5 + Mockito)** del `LoteTostadoService`:
  - cálculo de `mermaPorcentaje`,
  - rechazo cuando `salida ≥ entrada`,
  - rechazo cuando `entrada > stock`,
  - descuento de stock y transición a `AGOTADO`,
  - devolución de stock al anular.
- Verificación manual de la API vía Swagger UI.

(CI/CD, cobertura y E2E quedan fuera de alcance por decisión de scope.)

## 7. Estructura del repositorio

```
prueba-tecnica/
├── backend/
│   ├── src/main/java/com/cafe/trazabilidad/...
│   ├── src/main/resources/ (application.yml, db/migration)
│   ├── src/test/java/...
│   └── pom.xml
├── frontend/
│   ├── src/app/...
│   ├── tailwind.config.js
│   └── package.json
├── docs/
│   └── superpowers/specs/2026-06-16-cafe-trazabilidad-crud-design.md
└── README.md   (requisitos, cómo levantar back/front/BD, credenciales demo, capturas)
```

## 8. Criterios de aceptación

1. CRUD completo y funcional de **Finca**, **LoteCafeVerde** y **LoteTostado**
   desde la interfaz Angular contra la API Spring Boot y PostgreSQL.
2. Las reglas de negocio del tostado (merma, validación entrada/salida, control de
   stock y estados) se aplican en el backend y se reflejan en la UI.
3. Login con JWT funcional; ADMIN puede escribir, USER solo leer (verificable en UI y API).
4. Swagger UI accesible y documenta todos los endpoints.
5. Flyway crea el esquema y carga datos semilla al arrancar.
6. Tests unitarios del servicio de tostado en verde.
7. `README` permite a un evaluador levantar el proyecto siguiendo los pasos.
