# ☕ cafecito nas - proyecto — Trazabilidad de Café de Especialidad

Mantenimiento (CRUD) full‑stack que modela la trazabilidad real del café **"de la finca a la taza"**:

```
Finca (origen)  →  Lote de café verde  →  Lote tostado
```

Cada eslabón se registra con sus reglas de negocio: control de **stock**, cálculo de **merma de tueste**,
estados y **anulación con devolución de stock**. Incluye **autenticación JWT por roles** (ADMIN / USER).

> El frontend Angular viene **compilado y empaquetado dentro del backend**: al arrancar, **todo (app + API)
> se sirve desde una única URL: `http://localhost:8080`**.

![Login](docs/screenshots/01-login.png)

---

## 🧱 Stack

| Capa | Tecnología |
|---|---|
| **Frontend** | Angular 20 (standalone + signals) · TypeScript · **Tailwind CSS** (sistema de diseño propio) |
| **Backend** | Java 21 · Spring Boot 3.3 · arquitectura por capas (Controller → Service → Repository) |
| **Seguridad** | Spring Security + **JWT** (HS256, BCrypt) · roles `ADMIN` / `USER` |
| **Persistencia** | Spring Data JPA · **Flyway** (migraciones versionadas) |
| **Base de datos** | PostgreSQL 16 |
| **Mapeo / Tests** | MapStruct · JUnit 5 + Mockito |

---

## ✨ Mejoras incorporadas (más allá del CRUD base)

- **Spring Security + JWT** con autorización por rol: `ADMIN` escribe, `USER` solo lee (verificable en UI y API).
- **Reglas de negocio reales** en el servidor: merma `= (entrada − salida) / entrada × 100`, control de stock del
  lote verde, transición a `AGOTADO`, y anulación que **devuelve el peso** al stock.
- **Flyway** para esquema + datos semilla reproducibles.
- **Manejo global de errores** con respuesta JSON consistente y códigos correctos (400 / 401 / 403 / 404 / 409).
- **App empaquetada en un solo artefacto:** el frontend se sirve desde el backend (mismo origen, sin CORS,
  **una sola URL**), de modo que basta Java + Maven para ejecutarlo todo.
- **Diseño de UI intencional** (no plantilla genérica): identidad de tostaduría, tipografía Space Grotesk +
  IBM Plex Mono, y un **espectro de tueste** funcional que codifica los perfiles Light / Medium / Dark.

---

## 📸 Vistas

| Dashboard | Lotes tostados |
|---|---|
| ![Dashboard](docs/screenshots/02-dashboard.png) | ![Tostados](docs/screenshots/03-tostados.png) |

---

## ✅ Requisitos

- **Java 21** y **Maven 3.9+**
- **PostgreSQL 16** accesible en `localhost`
- (Solo si vas a recompilar el frontend) **Node 20+** y **npm**

---

## 🚀 Puesta en marcha

### 1. Base de datos

```sql
CREATE DATABASE cafe_trazabilidad;
CREATE USER cafe WITH PASSWORD 'cafe';
GRANT ALL PRIVILEGES ON DATABASE cafe_trazabilidad TO cafe;
```

> **Puerto:** por defecto el backend usa `5432`. Si tu PostgreSQL escucha en otro puerto, pásalo con la variable
> `DB_PORT`. También puedes ajustar `DB_USER` / `DB_PASSWORD` (o editar `backend/src/main/resources/application.yml`).

### 2. Arrancar (todo en una sola URL)

```bash
cd backend
mvn -DskipTests package
java -jar target/trazabilidad-1.0.0.jar
# Si tu Postgres usa otro puerto:  DB_PORT=5433 java -jar target/trazabilidad-1.0.0.jar
```

Abre **http://localhost:8080** y tienes:

- La **aplicación** (Angular) en la raíz.
- La **API** REST bajo `/api/**`.

**Flyway** crea el esquema y carga los datos demo en el primer arranque.

> Se usa el **jar empaquetado** (autocontenido) porque es fiable en cualquier ruta. `mvn spring-boot:run` también
> sirve si la ruta del proyecto **no contiene espacios** (en Windows, un espacio en la ruta rompe el classpath que
> genera el plugin).

### Desarrollo del frontend (opcional)

Para iterar el frontend con recarga en vivo:

```bash
cd frontend
npm install
npm start          # http://localhost:4200, con proxy de /api → :8080
```

Para regenerar el bundle que sirve el backend: `npm run build` y copiar `dist/frontend/browser/*` a
`backend/src/main/resources/static/`.

---

## 🔐 Credenciales demo

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | CRUD completo |
| `user`  | `user123`  | USER  | Solo lectura |

---

## 📐 Reglas de negocio

- **Merma de tueste:** calculada en el backend, nunca aceptada del cliente.
- **Control de stock:** al registrar un tueste se descuenta `pesoEntradaKg` del lote verde; si llega a 0 pasa a `AGOTADO`.
- **Validaciones:** `pesoSalidaKg < pesoEntradaKg` y `pesoEntradaKg ≤ stock` (devuelven `409`).
- **Anulación:** un tueste anulado **devuelve** el peso al lote verde y recalcula su estado.
- **Integridad referencial:** no se elimina una finca/lote con dependencias (`409`).

---

## 🗂️ Estructura

```
.
├── backend/                      # API Spring Boot (también sirve el frontend compilado)
│   └── src/main/
│       ├── java/com/cafe/trazabilidad/
│       │   ├── config/           # CORS
│       │   ├── common/           # errores globales, ApiError, PageResponse, BaseEntity
│       │   ├── security/         # JWT, SecurityConfig, login, usuarios
│       │   ├── finca/  loteverde/  lotetostado/   # features CRUD
│       │   ├── dashboard/        # endpoint de resumen
│       │   └── web/              # reenvío de rutas del SPA a index.html
│       └── resources/static/     # frontend Angular compilado (servido por el backend)
├── frontend/                     # SPA Angular 20 + Tailwind (sistema de diseño propio)
│   └── src/app/                  # core, shared, layout, features
├── docs/
│   └── screenshots/              # capturas de la UI
└── README.md
```

---

## 🧪 Tests

```bash
cd backend && mvn test
```

Tests unitarios (JUnit 5 + Mockito) centrados en la lógica de negocio: firma/validación JWT, reglas de borrado de
finca, y el núcleo del tostado (merma, validación entrada/salida, descuento de stock, anulación).

---

## 🛡️ Nota de seguridad (producción)

Este repositorio prioriza que un evaluador pueda **clonar y ejecutar sin configuración**: por eso incluye usuarios
semilla, un secreto JWT por defecto y credenciales de BD locales. Para un despliegue real:

- Inyectar `JWT_SECRET`, `DB_USER` y `DB_PASSWORD` por variables de entorno / gestor de secretos (sin valores por defecto).
- Mover los usuarios semilla a una migración gateada por perfil (`dev`) y forzar el cambio de contraseña inicial.
