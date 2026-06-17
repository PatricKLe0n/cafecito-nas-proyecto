# cafecito nas - proyecto

Aplicación web para llevar la trazabilidad de café de especialidad: desde la finca de origen
hasta el lote ya tostado. En el fondo es un CRUD con tres entidades encadenadas
(finca → lote de café verde → lote tostado) y algunas reglas propias del proceso, como el
control de stock, el cálculo de la merma de tueste y la anulación de tandas.

Está hecho con Angular en el frontend, Spring Boot (Java 21) en el backend y PostgreSQL como
base de datos. El acceso se controla con JWT y dos roles.

![Pantalla de login](docs/screenshots/01-login.png)

## Stack

- Frontend: Angular 20 (componentes standalone, signals) con Tailwind CSS.
- Backend: Java 21 y Spring Boot 3.3, organizado por capas (controlador → servicio → repositorio).
- Seguridad: Spring Security con JWT (BCrypt) y roles ADMIN / USER.
- Persistencia: Spring Data JPA y Flyway para las migraciones.
- Base de datos: PostgreSQL 16.
- Apoyo: MapStruct para el mapeo a DTOs y JUnit 5 + Mockito para los tests.

## Requisitos

- Java 21 y Maven 3.9 o superior.
- PostgreSQL 16.
- Node 20+ y npm, solo si vas a recompilar el frontend.

## Cómo ejecutarlo

1. Crear la base de datos:

```sql
CREATE DATABASE cafe_trazabilidad;
CREATE USER cafe WITH PASSWORD 'cafe';
GRANT ALL PRIVILEGES ON DATABASE cafe_trazabilidad TO cafe;
```

2. Compilar y arrancar el backend:

```bash
cd backend
mvn -DskipTests package
java -jar target/trazabilidad-1.0.0.jar
```

El frontend ya viene compilado dentro del backend, así que con eso es suficiente: abre
http://localhost:8080 y ahí tienes la aplicación y la API.

Si tu PostgreSQL escucha en otro puerto, pásalo con la variable `DB_PORT`, por ejemplo
`DB_PORT=5433 java -jar target/trazabilidad-1.0.0.jar`. En el primer arranque, Flyway crea el
esquema y carga algunos datos de ejemplo.

Usuarios de prueba:

- `admin` / `admin123` — puede crear, editar y borrar.
- `user` / `user123` — solo lectura.

### Frontend en desarrollo (opcional)

Si quieres tocar el frontend con recarga en caliente:

```bash
cd frontend
npm install
npm start
```

Queda en http://localhost:4200 y redirige las llamadas de `/api` al backend. Para volver a
empaquetarlo dentro del backend: `npm run build` y copiar el contenido de
`dist/frontend/browser/` a `backend/src/main/resources/static/`.

## Qué hace

- CRUD de fincas, lotes de café verde y lotes tostados.
- Al registrar un tostado se descuenta el peso del lote de café verde; cuando llega a cero, el
  lote pasa a "agotado".
- La merma de cada tostado la calcula el servidor: `(peso de entrada − peso de salida) / peso de entrada`.
- Anular un tostado devuelve el peso al lote verde.
- No se puede borrar una finca o un lote que tenga registros que dependan de él.
- Login con JWT; las operaciones de escritura quedan reservadas al rol ADMIN.
- Un panel con indicadores y unos gráficos: distribución por perfil de tueste, merma por lote y
  stock de café verde por finca.

## Estructura

```
backend/    API Spring Boot (también sirve el frontend compilado)
  src/main/java/com/cafe/trazabilidad/
    config/      configuración (CORS)
    common/      manejo de errores, paginación y clase base de entidades
    security/    JWT, configuración de seguridad y login
    finca/  loteverde/  lotetostado/   cada feature con su CRUD completo
    dashboard/   endpoints de métricas
    web/         reenvío de las rutas del SPA a index.html
  src/main/resources/
    db/migration/   migraciones Flyway (esquema y datos de ejemplo)
    static/         frontend ya compilado
frontend/   SPA Angular (core, shared, layout y features)
docs/screenshots/   capturas de la aplicación
```

## Tests

```bash
cd backend && mvn test
```

Son tests unitarios sobre la lógica de negocio: la generación y validación del JWT, las reglas
de borrado de finca y el núcleo del tostado (merma, validación de pesos, descuento y devolución
de stock).

## Notas

Para que sea fácil de probar, el proyecto incluye usuarios de ejemplo, un secreto de JWT por
defecto y la conexión a la base local ya configurada. En un entorno real esos valores deberían
ir en variables de entorno o en un gestor de secretos, y los usuarios de ejemplo habría que
desactivarlos.
