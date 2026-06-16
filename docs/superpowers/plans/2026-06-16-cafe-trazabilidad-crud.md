# CRUD Trazabilidad de Café de Especialidad — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir un CRUD full-stack de trazabilidad de café (Finca → LoteCafeVerde → LoteTostado) con Angular + Tailwind, Spring Boot + PostgreSQL, autenticación JWT por roles y documentación Swagger.

**Architecture:** Backend Spring Boot 3 / Java 21 en capas limpias (Controller → Service → Repository), DTOs con MapStruct, validación Bean Validation, errores globales con `@RestControllerAdvice`, migraciones Flyway y seguridad stateless JWT. Frontend Angular 18 standalone con Tailwind y componentes propios, interceptor JWT y guards de ruta. Monorepo `backend/` + `frontend/`.

**Tech Stack:** Java 21, Spring Boot 3.3, Spring Data JPA, Spring Security, JJWT, Flyway, MapStruct, Lombok, PostgreSQL 16, springdoc-openapi · Angular 18, TypeScript, Tailwind CSS, RxJS · JUnit 5 + Mockito.

**Spec de referencia:** `docs/superpowers/specs/2026-06-16-cafe-trazabilidad-crud-design.md`

---

## Convenciones del plan

- Comandos backend se ejecutan desde `backend/`; comandos frontend desde `frontend/`.
- Paquete raíz backend: `com.cafe.trazabilidad`.
- Cada tarea termina en commit. Mensajes en estilo Conventional Commits.
- Para correr el backend: PostgreSQL debe estar accesible con BD `cafe_trazabilidad` (ver Tarea 0.1). El puerto se configura con `DB_PORT` (por defecto 5432; **en esta máquina de desarrollo se usa 5433** porque el 5432 estaba ocupado). Ejemplo local: `DB_PORT=5433 mvn spring-boot:run`.

---

## Mapa de archivos (decisiones de descomposición)

**Backend** (`backend/src/main/java/com/cafe/trazabilidad/`):
```
TrazabilidadApplication.java         · arranque
config/    OpenApiConfig, CorsConfig
common/    BaseEntity, ApiError, GlobalExceptionHandler, RecursoNoEncontradoException,
           ReglaNegocioException, PageResponse
security/  Usuario, Rol, UsuarioRepository, JwtService, JwtAuthFilter, SecurityConfig,
           AppUserDetailsService, AuthController, dto/(LoginRequest, LoginResponse)
finca/     Finca, ProcesoBeneficio, FincaRepository, FincaService, FincaController,
           dto/(FincaRequest, FincaResponse), FincaMapper
loteverde/ LoteCafeVerde, EstadoLoteVerde, LoteVerdeRepository, LoteVerdeService,
           LoteVerdeController, dto/(LoteVerdeRequest, LoteVerdeResponse), LoteVerdeMapper
lotetostado/ LoteTostado, PerfilTueste, EstadoTostado, LoteTostadoRepository,
           LoteTostadoService, LoteTostadoController,
           dto/(LoteTostadoRequest, LoteTostadoResponse), LoteTostadoMapper
dashboard/ DashboardController, DashboardService, dto/ResumenResponse
```

**Frontend** (`frontend/src/app/`):
```
core/      models/, services/(auth, api base), interceptors/token.interceptor,
           guards/auth.guard, guards/admin.guard
shared/    components/(tabla-datos, paginador, modal-confirmacion, badge, campo-form)
layout/    shell (topbar + sidebar)
features/  auth/login, dashboard, fincas, lotes-verdes, lotes-tostados
```

---

# FASE 0 — Scaffolding

## Task 0.1: Proyecto backend Spring Boot (Maven)

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/cafe/trazabilidad/TrazabilidadApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/.gitignore` (heredado del raíz; no crear)

- [ ] **Step 1: Crear `backend/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.cafe</groupId>
    <artifactId>trazabilidad</artifactId>
    <version>1.0.0</version>
    <name>trazabilidad-cafe</name>
    <description>CRUD de trazabilidad de café de especialidad</description>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Crear `TrazabilidadApplication.java`**

```java
package com.cafe.trazabilidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrazabilidadApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrazabilidadApplication.class, args);
    }
}
```

- [ ] **Step 3: Crear `application.yml`**

```yaml
server:
  port: 8080

spring:
  application:
    name: trazabilidad-cafe
  datasource:
    url: jdbc:postgresql://localhost:${DB_PORT:5432}/cafe_trazabilidad
    username: ${DB_USER:cafe}
    password: ${DB_PASSWORD:cafe}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate.format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true

app:
  jwt:
    secret: ${JWT_SECRET:Y2FmZS10cmF6YWJpbGlkYWQtc2VjcmV0by1kZW1vLTI1Ni1iaXRzLW1pbmltbw==}
    issuer: trazabilidad-cafe
    expiration-minutes: 120
  cors:
    allowed-origin: ${FRONTEND_ORIGIN:http://localhost:4200}

springdoc:
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    org.hibernate.SQL: debug
```

- [ ] **Step 4: Verificar que compila**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS (la conexión a BD aún no se prueba en compile).

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml backend/src
git commit -m "chore(backend): scaffold proyecto Spring Boot con dependencias base"
```

---

## Task 0.2: Base de datos PostgreSQL + migraciones Flyway

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__schema.sql`
- Create: `backend/src/main/resources/db/migration/V2__seed.sql`

> Requisito de entorno: crear la BD una vez. Con psql:
> `CREATE DATABASE cafe_trazabilidad;` y un rol `cafe`/`cafe` con permisos, o ajusta `application.yml`.
> En esta máquina ya está hecho: servidor en `localhost:5433`, rol `cafe`/`cafe` (superuser),
> base `cafe_trazabilidad`. Para arrancar el backend localmente exporta `DB_PORT=5433`.

- [ ] **Step 1: Crear `V1__schema.sql`**

```sql
-- Usuarios y roles
CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(60)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    rol         VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'USER')),
    creado_en   TIMESTAMP    NOT NULL DEFAULT now()
);

-- Finca (origen)
CREATE TABLE finca (
    id            BIGSERIAL PRIMARY KEY,
    pais          VARCHAR(80)  NOT NULL,
    region        VARCHAR(80)  NOT NULL,
    nombre        VARCHAR(120) NOT NULL UNIQUE,
    productor     VARCHAR(120),
    altitud_msnm  INTEGER CHECK (altitud_msnm BETWEEN 0 AND 4000),
    variedad      VARCHAR(80),
    proceso       VARCHAR(20) NOT NULL CHECK (proceso IN ('LAVADO', 'NATURAL', 'HONEY')),
    creado_en     TIMESTAMP NOT NULL DEFAULT now()
);

-- Lote de café verde
CREATE TABLE lote_cafe_verde (
    id                  BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(40) NOT NULL UNIQUE,
    finca_id            BIGINT NOT NULL REFERENCES finca(id),
    peso_kg             NUMERIC(10,2) NOT NULL CHECK (peso_kg >= 0),
    humedad_porcentaje  NUMERIC(5,2)  CHECK (humedad_porcentaje BETWEEN 0 AND 100),
    puntaje_sca         NUMERIC(5,2)  CHECK (puntaje_sca BETWEEN 0 AND 100),
    fecha_recepcion     DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('DISPONIBLE', 'AGOTADO')),
    creado_en           TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_lote_verde_finca ON lote_cafe_verde(finca_id);

-- Lote tostado
CREATE TABLE lote_tostado (
    id                  BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(40) NOT NULL UNIQUE,
    lote_verde_id       BIGINT NOT NULL REFERENCES lote_cafe_verde(id),
    perfil_tueste       VARCHAR(20) NOT NULL CHECK (perfil_tueste IN ('LIGHT', 'MEDIUM', 'DARK')),
    peso_entrada_kg     NUMERIC(10,2) NOT NULL CHECK (peso_entrada_kg > 0),
    peso_salida_kg      NUMERIC(10,2) NOT NULL CHECK (peso_salida_kg > 0),
    merma_porcentaje    NUMERIC(5,2)  NOT NULL,
    fecha_tueste        TIMESTAMP NOT NULL,
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('REGISTRADO', 'ANULADO')),
    creado_en           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_salida_menor_entrada CHECK (peso_salida_kg < peso_entrada_kg)
);
CREATE INDEX idx_lote_tostado_verde ON lote_tostado(lote_verde_id);
```

- [ ] **Step 2: Crear `V2__seed.sql`** (hashes BCrypt de `admin123` / `user123`)

```sql
-- Contraseñas demo: admin123 y user123 (BCrypt fuerza 10, verificadas)
INSERT INTO usuario (username, password, rol) VALUES
 ('admin', '$2b$10$NUuSY.FC87W/vthhfVnPRe1oFU9meLixGpcnoKbEF8iZsLui/xs9W', 'ADMIN'),
 ('user',  '$2b$10$9z83hMZxztYsr28OTGJ9/OOfdocbjo1mZ2XG8Jg7wL7aFFJd3nL5u', 'USER');

INSERT INTO finca (pais, region, nombre, productor, altitud_msnm, variedad, proceso) VALUES
 ('Colombia', 'Huila',  'Finca El Mirador',  'Ana Gómez',   1750, 'Caturra', 'LAVADO'),
 ('Etiopía',  'Yirgacheffe', 'Konga Coop',  'Konga',       1950, 'Heirloom', 'NATURAL'),
 ('Perú',     'Cajamarca', 'Finca La Palma', 'José Ruiz',   1600, 'Bourbon', 'HONEY');

INSERT INTO lote_cafe_verde (codigo, finca_id, peso_kg, humedad_porcentaje, puntaje_sca, fecha_recepcion, estado) VALUES
 ('LV-2026-001', 1, 60.00, 10.5, 86.50, '2026-02-10', 'DISPONIBLE'),
 ('LV-2026-002', 2, 50.00, 11.0, 88.00, '2026-02-15', 'DISPONIBLE'),
 ('LV-2026-003', 3, 45.00, 10.0, 85.00, '2026-03-01', 'DISPONIBLE');

INSERT INTO lote_tostado (codigo, lote_verde_id, perfil_tueste, peso_entrada_kg, peso_salida_kg, merma_porcentaje, fecha_tueste, estado) VALUES
 ('LT-2026-001', 1, 'MEDIUM', 10.00, 8.50, 15.00, '2026-03-05 09:30:00', 'REGISTRADO');
-- Ajuste de stock del lote verde 1 por el tostado anterior:
UPDATE lote_cafe_verde SET peso_kg = peso_kg - 10.00 WHERE id = 1;
```

> Nota para el implementador: si el arranque falla por hash inválido, regenera los BCrypt con
> `new BCryptPasswordEncoder().encode("admin123")` y reemplaza en este archivo antes del primer arranque.
> Flyway aplica las migraciones una sola vez; ante cambios usa `mvn flyway:clean` (solo en dev) o un nuevo `V3__`.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db
git commit -m "feat(backend): esquema Flyway y datos semilla de trazabilidad"
```

---

# FASE 1 — Infraestructura común

## Task 1.1: Clases base, errores y respuesta paginada

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/BaseEntity.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/RecursoNoEncontradoException.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/ReglaNegocioException.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/ApiError.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/common/PageResponse.java`

- [ ] **Step 1: `BaseEntity.java`**

```java
package com.cafe.trazabilidad.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Excepciones de dominio**

```java
// RecursoNoEncontradoException.java
package com.cafe.trazabilidad.common;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado con id " + id);
    }
}
```

```java
// ReglaNegocioException.java
package com.cafe.trazabilidad.common;

public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
```

- [ ] **Step 3: `ApiError.java`**

```java
package com.cafe.trazabilidad.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
```

- [ ] **Step 4: `GlobalExceptionHandler.java`**

```java
package com.cafe.trazabilidad.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.FieldError> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError).toList();
        return build(HttpStatus.BAD_REQUEST, "Errores de validación", req, errores);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado", req, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req, List.of());
    }

    private ApiError.FieldError toFieldError(FieldError fe) {
        return new ApiError.FieldError(fe.getField(), fe.getDefaultMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String msg,
                                           HttpServletRequest req, List<ApiError.FieldError> fields) {
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(),
                status.getReasonPhrase(), msg, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
```

- [ ] **Step 5: `PageResponse.java`**

```java
package com.cafe.trazabilidad.common;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
        List<T> content, int page, int size,
        long totalElements, int totalPages, boolean first, boolean last
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }
}
```

- [ ] **Step 6: Verificar compilación y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/common
git commit -m "feat(backend): infraestructura común (errores, ApiError, PageResponse, BaseEntity)"
```

---

# FASE 2 — Seguridad (JWT)

## Task 2.1: Usuario, Rol y repositorio

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/Rol.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/Usuario.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/UsuarioRepository.java`

- [ ] **Step 1: `Rol.java`**

```java
package com.cafe.trazabilidad.security;

public enum Rol { ADMIN, USER }
```

- [ ] **Step 2: `Usuario.java`**

```java
package com.cafe.trazabilidad.security;

import com.cafe.trazabilidad.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
}
```

- [ ] **Step 3: `UsuarioRepository.java`**

```java
package com.cafe.trazabilidad.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/security
git commit -m "feat(security): entidad Usuario, Rol y repositorio"
```

---

## Task 2.2: JwtService (TDD)

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/JwtService.java`
- Test: `backend/src/test/java/com/cafe/trazabilidad/security/JwtServiceTest.java`

- [ ] **Step 1: Escribir el test que falla**

```java
package com.cafe.trazabilidad.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwt;

    @BeforeEach
    void setUp() {
        // secreto base64 de >=32 bytes, issuer y 60 min de expiración
        jwt = new JwtService(
            "Y2FmZS10cmF6YWJpbGlkYWQtc2VjcmV0by1kZW1vLTI1Ni1iaXRzLW1pbmltbw==",
            "trazabilidad-cafe", 60);
    }

    @Test
    void generaYValidaTokenConUsuarioYRol() {
        String token = jwt.generarToken("admin", "ADMIN");

        assertThat(jwt.esValido(token)).isTrue();
        assertThat(jwt.extraerUsername(token)).isEqualTo("admin");
        assertThat(jwt.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void tokenManipuladoEsInvalido() {
        String token = jwt.generarToken("admin", "ADMIN");
        assertThat(jwt.esValido(token + "x")).isFalse();
    }
}
```

- [ ] **Step 2: Ejecutar y verificar que falla**

Run: `cd backend && mvn -q -Dtest=JwtServiceTest test`
Expected: FAIL — no existe la clase `JwtService`.

- [ ] **Step 3: Implementar `JwtService.java`**

```java
package com.cafe.trazabilidad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secretBase64,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.issuer = issuer;
        this.expirationMillis = expirationMinutes * 60_000;
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public boolean esValido(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extraerUsername(String token) {
        return parse(token).getSubject();
    }

    public String extraerRol(String token) {
        return parse(token).get("rol", String.class);
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 4: Ejecutar y verificar que pasa**

Run: `cd backend && mvn -q -Dtest=JwtServiceTest test`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/security/JwtService.java backend/src/test
git commit -m "feat(security): JwtService con firma HS256 y validación (TDD)"
```

---

## Task 2.3: UserDetailsService, filtro JWT, SecurityConfig y AuthController

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/AppUserDetailsService.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/JwtAuthFilter.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/dto/LoginResponse.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/security/AuthController.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/config/CorsConfig.java`

- [ ] **Step 1: `AppUserDetailsService.java`**

```java
package com.cafe.trazabilidad.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public AppUserDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .build();
    }
}
```

- [ ] **Step 2: `JwtAuthFilter.java`**

```java
package com.cafe.trazabilidad.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.esValido(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extraerUsername(token);
                String rol = jwtService.extraerRol(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 3: `SecurityConfig.java`**

```java
package com.cafe.trazabilidad.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (request, response, ex) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado")))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
```

- [ ] **Step 4: DTOs de login**

```java
// LoginRequest.java
package com.cafe.trazabilidad.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {}
```

```java
// LoginResponse.java
package com.cafe.trazabilidad.security.dto;

public record LoginResponse(String token, String username, String rol, long expiraEnMs) {}
```

- [ ] **Step 5: `AuthController.java`**

```java
package com.cafe.trazabilidad.security;

import com.cafe.trazabilidad.security.dto.LoginRequest;
import com.cafe.trazabilidad.security.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UsuarioRepository usuarioRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        Usuario u = usuarioRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generarToken(u.getUsername(), u.getRol().name());
        return new LoginResponse(token, u.getUsername(), u.getRol().name(), jwtService.getExpirationMillis());
    }
}
```

- [ ] **Step 6: `CorsConfig.java`**

```java
package com.cafe.trazabilidad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigin));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
```

- [ ] **Step 7: Arrancar la app y probar login**

Run: `cd backend && mvn -q spring-boot:run` (en otra terminal)
Run: `curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'`
Expected: JSON con `token`, `username=admin`, `rol=ADMIN`. Si falla la contraseña, regenerar el hash BCrypt (ver Task 0.2 Step 2). Detener con Ctrl+C.

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/security backend/src/main/java/com/cafe/trazabilidad/config
git commit -m "feat(security): JWT filter, SecurityConfig por roles, login y CORS"
```

---

# FASE 3 — Feature Finca

## Task 3.1: Entidad Finca, repositorio, DTOs y mapper

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/ProcesoBeneficio.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/Finca.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/FincaRepository.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/dto/FincaRequest.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/dto/FincaResponse.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/FincaMapper.java`

- [ ] **Step 1: `ProcesoBeneficio.java`**

```java
package com.cafe.trazabilidad.finca;

public enum ProcesoBeneficio { LAVADO, NATURAL, HONEY }
```

- [ ] **Step 2: `Finca.java`**

```java
package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "finca")
public class Finca extends BaseEntity {
    @Column(nullable = false) private String pais;
    @Column(nullable = false) private String region;
    @Column(nullable = false, unique = true) private String nombre;
    private String productor;
    @Column(name = "altitud_msnm") private Integer altitudMsnm;
    private String variedad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcesoBeneficio proceso;
}
```

- [ ] **Step 3: `FincaRepository.java`**

```java
package com.cafe.trazabilidad.finca;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FincaRepository extends JpaRepository<Finca, Long> {
    Page<Finca> findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(
            String nombre, String region, Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
}
```

- [ ] **Step 4: DTOs**

```java
// FincaRequest.java
package com.cafe.trazabilidad.finca.dto;

import jakarta.validation.constraints.*;

public record FincaRequest(
        @NotBlank String pais,
        @NotBlank String region,
        @NotBlank String nombre,
        String productor,
        @Min(0) @Max(4000) Integer altitudMsnm,
        String variedad,
        @NotNull com.cafe.trazabilidad.finca.ProcesoBeneficio proceso) {}
```

```java
// FincaResponse.java
package com.cafe.trazabilidad.finca.dto;

import com.cafe.trazabilidad.finca.ProcesoBeneficio;

public record FincaResponse(
        Long id, String pais, String region, String nombre, String productor,
        Integer altitudMsnm, String variedad, ProcesoBeneficio proceso) {}
```

- [ ] **Step 5: `FincaMapper.java`**

```java
package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FincaMapper {
    FincaResponse toResponse(Finca entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    Finca toEntity(FincaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void update(@MappingTarget Finca entity, FincaRequest request);
}
```

- [ ] **Step 6: Compilar y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS (MapStruct genera el impl).

```bash
git add backend/src/main/java/com/cafe/trazabilidad/finca
git commit -m "feat(finca): entidad, repositorio, DTOs y mapper"
```

---

## Task 3.2: FincaService (TDD para la regla de borrado con dependencias)

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/FincaService.java`
- Modify: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeRepository.java` (se crea en Fase 4; aquí solo se usa el método `existsByFincaId`. Si aún no existe, crear interfaz mínima ahora y completarla en Fase 4.)
- Test: `backend/src/test/java/com/cafe/trazabilidad/finca/FincaServiceTest.java`

> Para evitar dependencia circular de fases, crea ya la interfaz `LoteVerdeRepository` con
> el método `existsByFincaId(Long)`; en la Fase 4 se le añaden el resto de métodos.

- [ ] **Step 1: Crear repositorio mínimo de lote verde (si no existe)**

```java
// backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeRepository.java
package com.cafe.trazabilidad.loteverde;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteVerdeRepository extends JpaRepository<LoteCafeVerde, Long> {
    boolean existsByFincaId(Long fincaId);
}
```

> Nota: requiere que exista la clase `LoteCafeVerde`. Si la Fase 4 no se ha hecho, crea un stub
> mínimo de la entidad ahora (solo `@Entity @Table(name="lote_cafe_verde")` con su `id`) y complétala
> en la Fase 4. **Recomendado:** ejecutar la Fase 4 (Task 4.1) ANTES que esta tarea para no usar stubs.

- [ ] **Step 2: Escribir el test que falla**

```java
package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FincaServiceTest {

    @Mock FincaRepository fincaRepository;
    @Mock LoteVerdeRepository loteVerdeRepository;
    @Mock FincaMapper mapper;
    @InjectMocks FincaService service;

    @Test
    void eliminarLanzaConflictoSiTieneLotesVerdes() {
        when(fincaRepository.existsById(1L)).thenReturn(true);
        when(loteVerdeRepository.existsByFincaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("lotes");
        verify(fincaRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarBorraSiNoTieneDependencias() {
        when(fincaRepository.existsById(1L)).thenReturn(true);
        when(loteVerdeRepository.existsByFincaId(1L)).thenReturn(false);

        service.eliminar(1L);

        verify(fincaRepository).deleteById(1L);
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        when(fincaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
```

- [ ] **Step 3: Ejecutar y verificar que falla**

Run: `cd backend && mvn -q -Dtest=FincaServiceTest test`
Expected: FAIL — `FincaService` no existe / métodos no definidos.

- [ ] **Step 4: Implementar `FincaService.java`**

```java
package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FincaService {

    private final FincaRepository fincaRepository;
    private final LoteVerdeRepository loteVerdeRepository;
    private final FincaMapper mapper;

    public FincaService(FincaRepository fincaRepository, LoteVerdeRepository loteVerdeRepository,
                        FincaMapper mapper) {
        this.fincaRepository = fincaRepository;
        this.loteVerdeRepository = loteVerdeRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FincaResponse> listar(String q, Pageable pageable) {
        Page<Finca> page = (q == null || q.isBlank())
                ? fincaRepository.findAll(pageable)
                : fincaRepository.findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public FincaResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public FincaResponse crear(FincaRequest req) {
        if (fincaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new ReglaNegocioException("Ya existe una finca con el nombre " + req.nombre());
        }
        Finca guardada = fincaRepository.save(mapper.toEntity(req));
        return mapper.toResponse(guardada);
    }

    @Transactional
    public FincaResponse actualizar(Long id, FincaRequest req) {
        Finca finca = buscar(id);
        mapper.update(finca, req);
        return mapper.toResponse(fincaRepository.save(finca));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!fincaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Finca", id);
        }
        if (loteVerdeRepository.existsByFincaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la finca: tiene lotes de café verde asociados");
        }
        fincaRepository.deleteById(id);
    }

    private Finca buscar(Long id) {
        return fincaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", id));
    }
}
```

- [ ] **Step 5: Ejecutar y verificar que pasa**

Run: `cd backend && mvn -q -Dtest=FincaServiceTest test`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/finca backend/src/main/java/com/cafe/trazabilidad/loteverde backend/src/test/java/com/cafe/trazabilidad/finca
git commit -m "feat(finca): FincaService con reglas de borrado y no-encontrado (TDD)"
```

---

## Task 3.3: FincaController

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/finca/FincaController.java`

- [ ] **Step 1: `FincaController.java`**

```java
package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fincas")
public class FincaController {

    private final FincaService service;

    public FincaController(FincaService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<FincaResponse> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.listar(q, pageable);
    }

    @GetMapping("/{id}")
    public FincaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FincaResponse crear(@Valid @RequestBody FincaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public FincaResponse actualizar(@PathVariable Long id, @Valid @RequestBody FincaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
```

- [ ] **Step 2: Compilar y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/finca/FincaController.java
git commit -m "feat(finca): controller REST con CRUD paginado"
```

---

# FASE 4 — Feature LoteCafeVerde

## Task 4.1: Entidad, enum, repositorio, DTOs y mapper

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/EstadoLoteVerde.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteCafeVerde.java`
- Modify: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeRepository.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/dto/LoteVerdeRequest.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/dto/LoteVerdeResponse.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeMapper.java`

- [ ] **Step 1: `EstadoLoteVerde.java`**

```java
package com.cafe.trazabilidad.loteverde;

public enum EstadoLoteVerde { DISPONIBLE, AGOTADO }
```

- [ ] **Step 2: `LoteCafeVerde.java`**

```java
package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.BaseEntity;
import com.cafe.trazabilidad.finca.Finca;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "lote_cafe_verde")
public class LoteCafeVerde extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finca_id")
    private Finca finca;

    @Column(name = "peso_kg", nullable = false)
    private BigDecimal pesoKg;

    @Column(name = "humedad_porcentaje")
    private BigDecimal humedadPorcentaje;

    @Column(name = "puntaje_sca")
    private BigDecimal puntajeSca;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLoteVerde estado;
}
```

- [ ] **Step 3: `LoteVerdeRepository.java` (completar)**

```java
package com.cafe.trazabilidad.loteverde;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteVerdeRepository extends JpaRepository<LoteCafeVerde, Long> {
    boolean existsByFincaId(Long fincaId);
    Page<LoteCafeVerde> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<LoteCafeVerde> findByEstado(EstadoLoteVerde estado, Pageable pageable);
    boolean existsByCodigoIgnoreCase(String codigo);
}
```

- [ ] **Step 4: DTOs**

```java
// LoteVerdeRequest.java
package com.cafe.trazabilidad.loteverde.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteVerdeRequest(
        @NotBlank String codigo,
        @NotNull Long fincaId,
        @NotNull @DecimalMin("0.0") BigDecimal pesoKg,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal humedadPorcentaje,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal puntajeSca,
        @NotNull @PastOrPresent LocalDate fechaRecepcion) {}
```

```java
// LoteVerdeResponse.java
package com.cafe.trazabilidad.loteverde.dto;

import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteVerdeResponse(
        Long id, String codigo, Long fincaId, String fincaNombre,
        BigDecimal pesoKg, BigDecimal humedadPorcentaje, BigDecimal puntajeSca,
        LocalDate fechaRecepcion, EstadoLoteVerde estado) {}
```

- [ ] **Step 5: `LoteVerdeMapper.java`**

```java
package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteVerdeMapper {

    @Mapping(target = "fincaId", source = "finca.id")
    @Mapping(target = "fincaNombre", source = "finca.nombre")
    LoteVerdeResponse toResponse(LoteCafeVerde entity);
}
```

- [ ] **Step 6: Compilar y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/loteverde
git commit -m "feat(lote-verde): entidad, repositorio, DTOs y mapper"
```

---

## Task 4.2: LoteVerdeService y Controller

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeService.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeController.java`
- Modify: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoRepository.java` (crear stub con `existsByLoteVerdeId`; se completa en Fase 5)

- [ ] **Step 1: Crear repositorio mínimo de tostado (para la regla de borrado)**

```java
// backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoRepository.java
package com.cafe.trazabilidad.lotetostado;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteTostadoRepository extends JpaRepository<LoteTostado, Long> {
    boolean existsByLoteVerdeId(Long loteVerdeId);
}
```

> Requiere la entidad `LoteTostado`. **Recomendado:** ejecutar Task 5.1 antes que esta. Si no,
> crear stub mínimo de `LoteTostado` y completarlo en Fase 5.

- [ ] **Step 2: `LoteVerdeService.java`**

```java
package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.finca.Finca;
import com.cafe.trazabilidad.finca.FincaRepository;
import com.cafe.trazabilidad.lotetostado.LoteTostadoRepository;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeRequest;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteVerdeService {

    private final LoteVerdeRepository repo;
    private final FincaRepository fincaRepository;
    private final LoteTostadoRepository tostadoRepository;
    private final LoteVerdeMapper mapper;

    public LoteVerdeService(LoteVerdeRepository repo, FincaRepository fincaRepository,
                            LoteTostadoRepository tostadoRepository, LoteVerdeMapper mapper) {
        this.repo = repo;
        this.fincaRepository = fincaRepository;
        this.tostadoRepository = tostadoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoteVerdeResponse> listar(String q, EstadoLoteVerde estado, Pageable pageable) {
        Page<LoteCafeVerde> page;
        if (estado != null) {
            page = repo.findByEstado(estado, pageable);
        } else if (q != null && !q.isBlank()) {
            page = repo.findByCodigoContainingIgnoreCase(q, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LoteVerdeResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public LoteVerdeResponse crear(LoteVerdeRequest req) {
        if (repo.existsByCodigoIgnoreCase(req.codigo())) {
            throw new ReglaNegocioException("Ya existe un lote verde con el código " + req.codigo());
        }
        Finca finca = fincaRepository.findById(req.fincaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", req.fincaId()));
        LoteCafeVerde lote = new LoteCafeVerde();
        aplicar(lote, req, finca);
        lote.setEstado(lote.getPesoKg().signum() > 0 ? EstadoLoteVerde.DISPONIBLE : EstadoLoteVerde.AGOTADO);
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public LoteVerdeResponse actualizar(Long id, LoteVerdeRequest req) {
        LoteCafeVerde lote = buscar(id);
        Finca finca = fincaRepository.findById(req.fincaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", req.fincaId()));
        aplicar(lote, req, finca);
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("LoteCafeVerde", id);
        }
        if (tostadoRepository.existsByLoteVerdeId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el lote verde: tiene lotes tostados asociados");
        }
        repo.deleteById(id);
    }

    private void aplicar(LoteCafeVerde lote, LoteVerdeRequest req, Finca finca) {
        lote.setCodigo(req.codigo());
        lote.setFinca(finca);
        lote.setPesoKg(req.pesoKg());
        lote.setHumedadPorcentaje(req.humedadPorcentaje());
        lote.setPuntajeSca(req.puntajeSca());
        lote.setFechaRecepcion(req.fechaRecepcion());
    }

    private LoteCafeVerde buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteCafeVerde", id));
    }
}
```

- [ ] **Step 3: `LoteVerdeController.java`**

```java
package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeRequest;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes-verdes")
public class LoteVerdeController {

    private final LoteVerdeService service;

    public LoteVerdeController(LoteVerdeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LoteVerdeResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoLoteVerde estado,
            @PageableDefault(size = 10, sort = "codigo") Pageable pageable) {
        return service.listar(q, estado, pageable);
    }

    @GetMapping("/{id}")
    public LoteVerdeResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteVerdeResponse crear(@Valid @RequestBody LoteVerdeRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public LoteVerdeResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteVerdeRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
```

- [ ] **Step 4: Compilar y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/loteverde backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoRepository.java
git commit -m "feat(lote-verde): service y controller CRUD con filtros"
```

---

# FASE 5 — Feature LoteTostado (núcleo de negocio)

## Task 5.1: Entidad, enums, DTOs y mapper

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/PerfilTueste.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/EstadoTostado.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostado.java`
- Modify: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoRepository.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/dto/LoteTostadoRequest.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/dto/LoteTostadoResponse.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoMapper.java`

- [ ] **Step 1: Enums**

```java
// PerfilTueste.java
package com.cafe.trazabilidad.lotetostado;
public enum PerfilTueste { LIGHT, MEDIUM, DARK }
```

```java
// EstadoTostado.java
package com.cafe.trazabilidad.lotetostado;
public enum EstadoTostado { REGISTRADO, ANULADO }
```

- [ ] **Step 2: `LoteTostado.java`**

```java
package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.BaseEntity;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lote_tostado")
public class LoteTostado extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_verde_id")
    private LoteCafeVerde loteVerde;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_tueste", nullable = false)
    private PerfilTueste perfilTueste;

    @Column(name = "peso_entrada_kg", nullable = false)
    private BigDecimal pesoEntradaKg;

    @Column(name = "peso_salida_kg", nullable = false)
    private BigDecimal pesoSalidaKg;

    @Column(name = "merma_porcentaje", nullable = false)
    private BigDecimal mermaPorcentaje;

    @Column(name = "fecha_tueste", nullable = false)
    private LocalDateTime fechaTueste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTostado estado;
}
```

- [ ] **Step 3: `LoteTostadoRepository.java` (completar)**

```java
package com.cafe.trazabilidad.lotetostado;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteTostadoRepository extends JpaRepository<LoteTostado, Long> {
    boolean existsByLoteVerdeId(Long loteVerdeId);
    Page<LoteTostado> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    Page<LoteTostado> findByPerfilTueste(PerfilTueste perfil, Pageable pageable);
    Page<LoteTostado> findByEstado(EstadoTostado estado, Pageable pageable);
    long countByEstado(EstadoTostado estado);
}
```

- [ ] **Step 4: DTOs**

```java
// LoteTostadoRequest.java
package com.cafe.trazabilidad.lotetostado.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteTostadoRequest(
        @NotBlank String codigo,
        @NotNull Long loteVerdeId,
        @NotNull com.cafe.trazabilidad.lotetostado.PerfilTueste perfilTueste,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pesoEntradaKg,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pesoSalidaKg,
        @NotNull @PastOrPresent LocalDateTime fechaTueste) {}
```

```java
// LoteTostadoResponse.java
package com.cafe.trazabilidad.lotetostado.dto;

import com.cafe.trazabilidad.lotetostado.EstadoTostado;
import com.cafe.trazabilidad.lotetostado.PerfilTueste;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteTostadoResponse(
        Long id, String codigo, Long loteVerdeId, String loteVerdeCodigo,
        PerfilTueste perfilTueste, BigDecimal pesoEntradaKg, BigDecimal pesoSalidaKg,
        BigDecimal mermaPorcentaje, LocalDateTime fechaTueste, EstadoTostado estado) {}
```

- [ ] **Step 5: `LoteTostadoMapper.java`**

```java
package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteTostadoMapper {

    @Mapping(target = "loteVerdeId", source = "loteVerde.id")
    @Mapping(target = "loteVerdeCodigo", source = "loteVerde.codigo")
    LoteTostadoResponse toResponse(LoteTostado entity);
}
```

- [ ] **Step 6: Compilar y commit**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/lotetostado
git commit -m "feat(lote-tostado): entidad, enums, DTOs y mapper"
```

---

## Task 5.2: LoteTostadoService — reglas de negocio (TDD)

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoService.java`
- Test: `backend/src/test/java/com/cafe/trazabilidad/lotetostado/LoteTostadoServiceTest.java`

Este es el corazón del dominio: cálculo de merma, validación entrada/salida, control de stock
del lote verde y anulación con devolución de stock.

- [ ] **Step 1: Escribir el test que falla**

```java
package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteTostadoServiceTest {

    @Mock LoteTostadoRepository repo;
    @Mock LoteVerdeRepository loteVerdeRepository;
    @Mock LoteTostadoMapper mapper;
    @InjectMocks LoteTostadoService service;

    private LoteCafeVerde verde;

    @BeforeEach
    void setUp() {
        verde = new LoteCafeVerde();
        verde.setId(1L);
        verde.setPesoKg(new BigDecimal("20.00"));
        verde.setEstado(EstadoLoteVerde.DISPONIBLE);
    }

    private LoteTostadoRequest req(String entrada, String salida) {
        return new LoteTostadoRequest("LT-X", 1L, PerfilTueste.MEDIUM,
                new BigDecimal(entrada), new BigDecimal(salida), LocalDateTime.now());
    }

    @Test
    void crearCalculaMermaYDescuentaStock() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<LoteTostado> captor = ArgumentCaptor.forClass(LoteTostado.class);
        service.crear(req("10.00", "8.50"));

        verify(repo).save(captor.capture());
        LoteTostado guardado = captor.getValue();
        // merma = (10 - 8.5) / 10 * 100 = 15.00
        assertThat(guardado.getMermaPorcentaje()).isEqualByComparingTo("15.00");
        assertThat(guardado.getEstado()).isEqualTo(EstadoTostado.REGISTRADO);
        // stock del verde: 20 - 10 = 10
        assertThat(verde.getPesoKg()).isEqualByComparingTo("10.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.DISPONIBLE);
    }

    @Test
    void crearAgotaElLoteVerdeCuandoStockLlegaACero() {
        verde.setPesoKg(new BigDecimal("10.00"));
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(req("10.00", "8.00"));

        assertThat(verde.getPesoKg()).isEqualByComparingTo("0.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.AGOTADO);
    }

    @Test
    void crearRechazaSiSalidaMayorOIgualQueEntrada() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        assertThatThrownBy(() -> service.crear(req("10.00", "10.00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("salida");
        verify(repo, never()).save(any());
    }

    @Test
    void crearRechazaSiEntradaSuperaElStock() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        assertThatThrownBy(() -> service.crear(req("25.00", "20.00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("stock");
        verify(repo, never()).save(any());
    }

    @Test
    void anularDevuelveStockYReactivaElLoteVerde() {
        verde.setPesoKg(new BigDecimal("0.00"));
        verde.setEstado(EstadoLoteVerde.AGOTADO);
        LoteTostado tostado = new LoteTostado();
        tostado.setId(5L);
        tostado.setLoteVerde(verde);
        tostado.setPesoEntradaKg(new BigDecimal("10.00"));
        tostado.setEstado(EstadoTostado.REGISTRADO);
        when(repo.findById(5L)).thenReturn(Optional.of(tostado));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        service.anular(5L);

        assertThat(tostado.getEstado()).isEqualTo(EstadoTostado.ANULADO);
        assertThat(verde.getPesoKg()).isEqualByComparingTo("10.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.DISPONIBLE);
    }

    @Test
    void anularDosVecesEsRechazado() {
        LoteTostado tostado = new LoteTostado();
        tostado.setId(5L);
        tostado.setLoteVerde(verde);
        tostado.setPesoEntradaKg(new BigDecimal("10.00"));
        tostado.setEstado(EstadoTostado.ANULADO);
        when(repo.findById(5L)).thenReturn(Optional.of(tostado));

        assertThatThrownBy(() -> service.anular(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("anulado");
    }
}
```

- [ ] **Step 2: Ejecutar y verificar que falla**

Run: `cd backend && mvn -q -Dtest=LoteTostadoServiceTest test`
Expected: FAIL — `LoteTostadoService` no existe.

- [ ] **Step 3: Implementar `LoteTostadoService.java`**

```java
package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoteTostadoService {

    private final LoteTostadoRepository repo;
    private final LoteVerdeRepository loteVerdeRepository;
    private final LoteTostadoMapper mapper;

    public LoteTostadoService(LoteTostadoRepository repo, LoteVerdeRepository loteVerdeRepository,
                              LoteTostadoMapper mapper) {
        this.repo = repo;
        this.loteVerdeRepository = loteVerdeRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoteTostadoResponse> listar(String q, PerfilTueste perfil,
                                                    EstadoTostado estado, Pageable pageable) {
        Page<LoteTostado> page;
        if (estado != null) {
            page = repo.findByEstado(estado, pageable);
        } else if (perfil != null) {
            page = repo.findByPerfilTueste(perfil, pageable);
        } else if (q != null && !q.isBlank()) {
            page = repo.findByCodigoContainingIgnoreCase(q, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LoteTostadoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public LoteTostadoResponse crear(LoteTostadoRequest req) {
        LoteCafeVerde verde = loteVerdeRepository.findById(req.loteVerdeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteCafeVerde", req.loteVerdeId()));

        if (req.pesoSalidaKg().compareTo(req.pesoEntradaKg()) >= 0) {
            throw new ReglaNegocioException("El peso de salida debe ser menor que el de entrada");
        }
        if (req.pesoEntradaKg().compareTo(verde.getPesoKg()) > 0) {
            throw new ReglaNegocioException("El peso de entrada supera el stock disponible del lote verde ("
                    + verde.getPesoKg() + " kg)");
        }

        LoteTostado lote = new LoteTostado();
        lote.setCodigo(req.codigo());
        lote.setLoteVerde(verde);
        lote.setPerfilTueste(req.perfilTueste());
        lote.setPesoEntradaKg(req.pesoEntradaKg());
        lote.setPesoSalidaKg(req.pesoSalidaKg());
        lote.setMermaPorcentaje(calcularMerma(req.pesoEntradaKg(), req.pesoSalidaKg()));
        lote.setFechaTueste(req.fechaTueste());
        lote.setEstado(EstadoTostado.REGISTRADO);

        descontarStock(verde, req.pesoEntradaKg());
        return mapper.toResponse(repo.save(lote));
    }

    /** Solo edita campos que no afectan al stock (perfil, código, fecha). */
    @Transactional
    public LoteTostadoResponse actualizar(Long id, LoteTostadoRequest req) {
        LoteTostado lote = buscar(id);
        if (lote.getEstado() == EstadoTostado.ANULADO) {
            throw new ReglaNegocioException("No se puede editar un lote tostado anulado");
        }
        lote.setCodigo(req.codigo());
        lote.setPerfilTueste(req.perfilTueste());
        lote.setFechaTueste(req.fechaTueste());
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public void anular(Long id) {
        LoteTostado lote = buscar(id);
        if (lote.getEstado() == EstadoTostado.ANULADO) {
            throw new ReglaNegocioException("El lote ya está anulado");
        }
        LoteCafeVerde verde = lote.getLoteVerde();
        verde.setPesoKg(verde.getPesoKg().add(lote.getPesoEntradaKg()));
        if (verde.getPesoKg().signum() > 0) {
            verde.setEstado(EstadoLoteVerde.DISPONIBLE);
        }
        lote.setEstado(EstadoTostado.ANULADO);
        repo.save(lote);
    }

    private BigDecimal calcularMerma(BigDecimal entrada, BigDecimal salida) {
        return entrada.subtract(salida)
                .multiply(BigDecimal.valueOf(100))
                .divide(entrada, 2, RoundingMode.HALF_UP);
    }

    private void descontarStock(LoteCafeVerde verde, BigDecimal cantidad) {
        BigDecimal restante = verde.getPesoKg().subtract(cantidad);
        verde.setPesoKg(restante);
        if (restante.signum() <= 0) {
            verde.setEstado(EstadoLoteVerde.AGOTADO);
        }
    }

    private LoteTostado buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteTostado", id));
    }
}
```

- [ ] **Step 4: Ejecutar y verificar que pasa**

Run: `cd backend && mvn -q -Dtest=LoteTostadoServiceTest test`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoService.java backend/src/test/java/com/cafe/trazabilidad/lotetostado
git commit -m "feat(lote-tostado): reglas de merma, control de stock y anulación (TDD)"
```

---

## Task 5.3: LoteTostadoController

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoController.java`

- [ ] **Step 1: `LoteTostadoController.java`**

```java
package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes-tostados")
public class LoteTostadoController {

    private final LoteTostadoService service;

    public LoteTostadoController(LoteTostadoService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LoteTostadoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PerfilTueste perfil,
            @RequestParam(required = false) EstadoTostado estado,
            @PageableDefault(size = 10, sort = "fechaTueste") Pageable pageable) {
        return service.listar(q, perfil, estado, pageable);
    }

    @GetMapping("/{id}")
    public LoteTostadoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteTostadoResponse crear(@Valid @RequestBody LoteTostadoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public LoteTostadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteTostadoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anular(@PathVariable Long id) {
        service.anular(id);
    }
}
```

- [ ] **Step 2: Ejecutar toda la suite de tests y arrancar**

Run: `cd backend && mvn -q test`
Expected: PASS (todos los tests).
Run: `cd backend && mvn -q spring-boot:run` y verifica en `http://localhost:8080/swagger-ui.html` que aparecen los 3 controllers. Detener con Ctrl+C.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/lotetostado/LoteTostadoController.java
git commit -m "feat(lote-tostado): controller REST (CRUD + anulación)"
```

---

# FASE 6 — Dashboard y OpenAPI

## Task 6.1: DashboardService, DTO y Controller

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/dashboard/dto/ResumenResponse.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/dashboard/DashboardService.java`
- Create: `backend/src/main/java/com/cafe/trazabilidad/dashboard/DashboardController.java`
- Modify: `backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeRepository.java` (añadir `countByEstado`)

- [ ] **Step 1: Añadir `countByEstado` a `LoteVerdeRepository`**

Añade este método a la interfaz `LoteVerdeRepository`:

```java
    long countByEstado(EstadoLoteVerde estado);
```

- [ ] **Step 2: `ResumenResponse.java`**

```java
package com.cafe.trazabilidad.dashboard.dto;

import java.math.BigDecimal;

public record ResumenResponse(
        long lotesVerdesDisponibles,
        long lotesTostadosRegistrados,
        BigDecimal mermaMediaPorcentaje,
        long totalFincas) {}
```

- [ ] **Step 3: `DashboardService.java`**

```java
package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import com.cafe.trazabilidad.finca.FincaRepository;
import com.cafe.trazabilidad.lotetostado.EstadoTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostadoRepository;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DashboardService {

    private final FincaRepository fincaRepository;
    private final LoteVerdeRepository loteVerdeRepository;
    private final LoteTostadoRepository loteTostadoRepository;

    public DashboardService(FincaRepository fincaRepository, LoteVerdeRepository loteVerdeRepository,
                            LoteTostadoRepository loteTostadoRepository) {
        this.fincaRepository = fincaRepository;
        this.loteVerdeRepository = loteVerdeRepository;
        this.loteTostadoRepository = loteTostadoRepository;
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        long verdesDisponibles = loteVerdeRepository.countByEstado(EstadoLoteVerde.DISPONIBLE);
        long tostadosRegistrados = loteTostadoRepository.countByEstado(EstadoTostado.REGISTRADO);
        long totalFincas = fincaRepository.count();

        List<LoteTostado> registrados = loteTostadoRepository.findAll().stream()
                .filter(t -> t.getEstado() == EstadoTostado.REGISTRADO)
                .toList();
        BigDecimal mermaMedia = registrados.isEmpty() ? BigDecimal.ZERO
                : registrados.stream().map(LoteTostado::getMermaPorcentaje)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(registrados.size()), 2, RoundingMode.HALF_UP);

        return new ResumenResponse(verdesDisponibles, tostadosRegistrados, mermaMedia, totalFincas);
    }
}
```

- [ ] **Step 4: `DashboardController.java`**

```java
package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return service.resumen();
    }
}
```

- [ ] **Step 5: Compilar, test y commit**

Run: `cd backend && mvn -q test`
Expected: PASS

```bash
git add backend/src/main/java/com/cafe/trazabilidad/dashboard backend/src/main/java/com/cafe/trazabilidad/loteverde/LoteVerdeRepository.java
git commit -m "feat(dashboard): endpoint de resumen con totales y merma media"
```

---

## Task 6.2: Configuración OpenAPI con seguridad JWT

**Files:**
- Create: `backend/src/main/java/com/cafe/trazabilidad/config/OpenApiConfig.java`

- [ ] **Step 1: `OpenApiConfig.java`**

```java
package com.cafe.trazabilidad.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API Trazabilidad de Café de Especialidad")
                        .version("1.0.0")
                        .description("CRUD de fincas, lotes verdes y lotes tostados con seguridad JWT."))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
```

- [ ] **Step 2: Verificar en Swagger UI**

Run: `cd backend && mvn -q spring-boot:run`
Abrir `http://localhost:8080/swagger-ui.html`, usar "Authorize" con el token del login y probar `GET /api/fincas`. Detener con Ctrl+C.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/cafe/trazabilidad/config/OpenApiConfig.java
git commit -m "feat(docs): OpenAPI con esquema de seguridad Bearer JWT"
```

---

# FASE 7 — Frontend Angular + Tailwind

> **Nota de ejecución (divergencia respecto al plan original):** el frontend se implementó en **Angular 20**
> (convenciones nuevas: clase `App`, `*.ts` sin sufijo `.component`, signal inputs, control‑flow `@if/@for`) por
> compatibilidad con Node 24, y con un **sistema de diseño propio "Roast Log"** (paleta papel/espresso + acento
> verdigris + espectro de tueste funcional; tipografías Space Grotesk / Inter / IBM Plex Mono) en lugar de la
> paleta genérica espresso/crema/ámbar que se describe abajo. La lógica (servicios, signals, interceptor JWT,
> guards, formularios reactivos, CRUD por rol) se mantiene fiel a las tareas siguientes. Ver `docs/screenshots/`.

## Task 7.1: Scaffold Angular + Tailwind

**Files:**
- Create: proyecto Angular en `frontend/`
- Create/Modify: `frontend/tailwind.config.js`, `frontend/src/styles.css`, `frontend/src/environments/`, `frontend/proxy.conf.json`

- [ ] **Step 1: Generar el proyecto Angular**

Run (desde la raíz `prueba-tecnica/`):
```bash
npx -y @angular/cli@18 new frontend --routing --style=css --ssr=false --skip-git --package-manager=npm
```
Expected: proyecto creado en `frontend/`.

- [ ] **Step 2: Instalar y configurar Tailwind**

Run:
```bash
cd frontend && npm install -D tailwindcss@3 postcss autoprefixer && npx tailwindcss init
```

Reemplaza `frontend/tailwind.config.js`:
```js
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        espresso: '#3B2417',
        crema: '#F5EFE6',
        ambar: '#C8893F',
        tostado: '#6F4E37',
        verde: '#5A7042',
      },
      fontFamily: {
        display: ['Georgia', 'serif'],
        sans: ['system-ui', 'Segoe UI', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
```

Reemplaza `frontend/src/styles.css`:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

html, body { height: 100%; }
body { @apply bg-crema text-espresso font-sans; }
```

- [ ] **Step 3: Configurar entorno y proxy hacia el backend**

Create `frontend/src/environments/environment.ts`:
```ts
export const environment = {
  production: false,
  apiUrl: '/api',
};
```

Create `frontend/proxy.conf.json`:
```json
{
  "/api": { "target": "http://localhost:8080", "secure": false, "changeOrigin": true }
}
```

Modifica `frontend/angular.json`: en `projects.frontend.architect.serve.options` añade:
```json
"proxyConfig": "proxy.conf.json"
```

- [ ] **Step 4: Verificar arranque**

Run: `cd frontend && npm start`
Expected: app servida en `http://localhost:4200` sin errores de compilación. Detener con Ctrl+C.

- [ ] **Step 5: Commit**

```bash
git add frontend
git commit -m "chore(frontend): scaffold Angular 18 + Tailwind + proxy al backend"
```

---

## Task 7.2: Core — modelos, servicios, interceptor y guards

**Files:**
- Create: `frontend/src/app/core/models/page.model.ts`
- Create: `frontend/src/app/core/models/auth.model.ts`
- Create: `frontend/src/app/core/models/finca.model.ts`
- Create: `frontend/src/app/core/models/lote-verde.model.ts`
- Create: `frontend/src/app/core/models/lote-tostado.model.ts`
- Create: `frontend/src/app/core/models/dashboard.model.ts`
- Create: `frontend/src/app/core/services/auth.service.ts`
- Create: `frontend/src/app/core/services/crud.service.ts`
- Create: `frontend/src/app/core/interceptors/token.interceptor.ts`
- Create: `frontend/src/app/core/guards/auth.guard.ts`
- Create: `frontend/src/app/core/guards/admin.guard.ts`

- [ ] **Step 1: Modelos**

```ts
// page.model.ts
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
```

```ts
// auth.model.ts
export interface LoginRequest { username: string; password: string; }
export interface LoginResponse { token: string; username: string; rol: 'ADMIN' | 'USER'; expiraEnMs: number; }
```

```ts
// finca.model.ts
export type Proceso = 'LAVADO' | 'NATURAL' | 'HONEY';
export interface Finca {
  id: number; pais: string; region: string; nombre: string;
  productor?: string; altitudMsnm?: number; variedad?: string; proceso: Proceso;
}
export type FincaRequest = Omit<Finca, 'id'>;
```

```ts
// lote-verde.model.ts
export type EstadoLoteVerde = 'DISPONIBLE' | 'AGOTADO';
export interface LoteVerde {
  id: number; codigo: string; fincaId: number; fincaNombre: string;
  pesoKg: number; humedadPorcentaje?: number; puntajeSca?: number;
  fechaRecepcion: string; estado: EstadoLoteVerde;
}
export interface LoteVerdeRequest {
  codigo: string; fincaId: number; pesoKg: number;
  humedadPorcentaje?: number; puntajeSca?: number; fechaRecepcion: string;
}
```

```ts
// lote-tostado.model.ts
export type PerfilTueste = 'LIGHT' | 'MEDIUM' | 'DARK';
export type EstadoTostado = 'REGISTRADO' | 'ANULADO';
export interface LoteTostado {
  id: number; codigo: string; loteVerdeId: number; loteVerdeCodigo: string;
  perfilTueste: PerfilTueste; pesoEntradaKg: number; pesoSalidaKg: number;
  mermaPorcentaje: number; fechaTueste: string; estado: EstadoTostado;
}
export interface LoteTostadoRequest {
  codigo: string; loteVerdeId: number; perfilTueste: PerfilTueste;
  pesoEntradaKg: number; pesoSalidaKg: number; fechaTueste: string;
}
```

```ts
// dashboard.model.ts
export interface Resumen {
  lotesVerdesDisponibles: number;
  lotesTostadosRegistrados: number;
  mermaMediaPorcentaje: number;
  totalFincas: number;
}
```

- [ ] **Step 2: `auth.service.ts`**

```ts
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'cafe_token';
const ROL_KEY = 'cafe_rol';
const USER_KEY = 'cafe_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _rol = signal<string | null>(localStorage.getItem(ROL_KEY));
  private readonly _user = signal<string | null>(localStorage.getItem(USER_KEY));

  readonly rol = this._rol.asReadonly();
  readonly user = this._user.asReadonly();
  readonly isAdmin = computed(() => this._rol() === 'ADMIN');
  readonly isAuthenticated = computed(() => !!this._user());

  constructor(private http: HttpClient) {}

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, req).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(ROL_KEY, res.rol);
        localStorage.setItem(USER_KEY, res.username);
        this._rol.set(res.rol);
        this._user.set(res.username);
      }),
    );
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  logout(): void {
    localStorage.clear();
    this._rol.set(null);
    this._user.set(null);
  }
}
```

- [ ] **Step 3: `crud.service.ts` (servicio CRUD genérico reutilizable)**

```ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';

export interface ListParams {
  page?: number; size?: number; sort?: string; q?: string;
  [key: string]: string | number | undefined;
}

export class CrudService<T, R> {
  constructor(protected http: HttpClient, protected baseUrl: string) {}

  list(params: ListParams = {}): Observable<Page<T>> {
    let hp = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') hp = hp.set(k, String(v));
    });
    return this.http.get<Page<T>>(this.baseUrl, { params: hp });
  }

  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${id}`);
  }

  create(body: R): Observable<T> {
    return this.http.post<T>(this.baseUrl, body);
  }

  update(id: number, body: R): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
```

- [ ] **Step 4: `token.interceptor.ts`**

```ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token;

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
```

- [ ] **Step 5: Guards**

```ts
// auth.guard.ts
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  router.navigate(['/login']);
  return false;
};
```

```ts
// admin.guard.ts
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAdmin()) return true;
  router.navigate(['/']);
  return false;
};
```

- [ ] **Step 6: Registrar interceptor en `app.config.ts`**

Reemplaza `frontend/src/app/app.config.ts`:
```ts
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { tokenInterceptor } from './core/interceptors/token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([tokenInterceptor])),
  ],
};
```

- [ ] **Step 7: Compilar y commit**

Run: `cd frontend && npm run build`
Expected: build sin errores.

```bash
git add frontend/src/app/core frontend/src/app/app.config.ts frontend/src/environments
git commit -m "feat(frontend): core (auth, crud service, interceptor JWT, guards, modelos)"
```

---

## Task 7.3: Componentes compartidos (shared)

**Files:**
- Create: `frontend/src/app/shared/components/badge.component.ts`
- Create: `frontend/src/app/shared/components/paginador.component.ts`
- Create: `frontend/src/app/shared/components/modal-confirmacion.component.ts`

- [ ] **Step 1: `badge.component.ts`**

```ts
import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="px-2 py-0.5 rounded-full text-xs font-semibold" [ngClass]="clase">{{ texto }}</span>`,
})
export class BadgeComponent {
  @Input() texto = '';
  @Input() tipo: 'verde' | 'ambar' | 'gris' | 'rojo' = 'gris';

  get clase(): string {
    return {
      verde: 'bg-verde/15 text-verde',
      ambar: 'bg-ambar/20 text-tostado',
      gris: 'bg-espresso/10 text-espresso',
      rojo: 'bg-red-100 text-red-700',
    }[this.tipo];
  }
}
```

- [ ] **Step 2: `paginador.component.ts`**

```ts
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-paginador',
  standalone: true,
  template: `
    <div class="flex items-center justify-between mt-4 text-sm">
      <span>{{ totalElements }} registros · página {{ page + 1 }} de {{ totalPages || 1 }}</span>
      <div class="flex gap-2">
        <button class="px-3 py-1 rounded border border-espresso/20 disabled:opacity-40"
                [disabled]="first" (click)="cambiar.emit(page - 1)">Anterior</button>
        <button class="px-3 py-1 rounded border border-espresso/20 disabled:opacity-40"
                [disabled]="last" (click)="cambiar.emit(page + 1)">Siguiente</button>
      </div>
    </div>`,
})
export class PaginadorComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() first = true;
  @Input() last = true;
  @Output() cambiar = new EventEmitter<number>();
}
```

- [ ] **Step 3: `modal-confirmacion.component.ts`**

```ts
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-modal-confirmacion',
  standalone: true,
  template: `
    @if (abierto) {
      <div class="fixed inset-0 bg-espresso/40 flex items-center justify-center z-50" (click)="cancelar.emit()">
        <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm" (click)="$event.stopPropagation()">
          <h3 class="font-display text-lg mb-2">{{ titulo }}</h3>
          <p class="text-sm text-espresso/80 mb-5">{{ mensaje }}</p>
          <div class="flex justify-end gap-2">
            <button class="px-4 py-2 rounded border border-espresso/20" (click)="cancelar.emit()">Cancelar</button>
            <button class="px-4 py-2 rounded bg-red-600 text-white" (click)="confirmar.emit()">Confirmar</button>
          </div>
        </div>
      </div>
    }`,
})
export class ModalConfirmacionComponent {
  @Input() abierto = false;
  @Input() titulo = 'Confirmar';
  @Input() mensaje = '¿Seguro?';
  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();
}
```

- [ ] **Step 4: Build y commit**

Run: `cd frontend && npm run build`
Expected: sin errores.

```bash
git add frontend/src/app/shared
git commit -m "feat(frontend): componentes compartidos (badge, paginador, modal de confirmación)"
```

---

## Task 7.4: Layout (shell) y rutas

**Files:**
- Create: `frontend/src/app/layout/shell.component.ts`
- Modify: `frontend/src/app/app.routes.ts`
- Modify: `frontend/src/app/app.component.ts` y `app.component.html`

- [ ] **Step 1: `shell.component.ts`**

```ts
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="min-h-screen flex">
      <aside class="w-60 bg-espresso text-crema flex flex-col">
        <div class="p-5 font-display text-xl border-b border-white/10">☕ Trazabilidad</div>
        <nav class="flex-1 p-3 space-y-1 text-sm">
          <a routerLink="/dashboard" routerLinkActive="bg-white/10"
             class="block px-3 py-2 rounded hover:bg-white/10">Resumen</a>
          <a routerLink="/fincas" routerLinkActive="bg-white/10"
             class="block px-3 py-2 rounded hover:bg-white/10">Fincas</a>
          <a routerLink="/lotes-verdes" routerLinkActive="bg-white/10"
             class="block px-3 py-2 rounded hover:bg-white/10">Lotes verdes</a>
          <a routerLink="/lotes-tostados" routerLinkActive="bg-white/10"
             class="block px-3 py-2 rounded hover:bg-white/10">Lotes tostados</a>
        </nav>
        <div class="p-3 border-t border-white/10 text-xs">
          <div class="mb-2 opacity-80">{{ auth.user() }} · {{ auth.rol() }}</div>
          <button class="w-full px-3 py-2 rounded bg-white/10 hover:bg-white/20" (click)="salir()">Salir</button>
        </div>
      </aside>
      <main class="flex-1 p-8 overflow-auto"><router-outlet /></main>
    </div>`,
})
export class ShellComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  salir() { this.auth.logout(); this.router.navigate(['/login']); }
}
```

- [ ] **Step 2: `app.component.ts` / `app.component.html`**

Reemplaza `frontend/src/app/app.component.ts`:
```ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class AppComponent {}
```

- [ ] **Step 3: `app.routes.ts`**

```ts
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ShellComponent } from './layout/shell.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'fincas', loadComponent: () => import('./features/fincas/fincas.component').then(m => m.FincasComponent) },
      { path: 'lotes-verdes', loadComponent: () => import('./features/lotes-verdes/lotes-verdes.component').then(m => m.LotesVerdesComponent) },
      { path: 'lotes-tostados', loadComponent: () => import('./features/lotes-tostados/lotes-tostados.component').then(m => m.LotesTostadosComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

- [ ] **Step 4: Commit** (la app aún no compila hasta crear los componentes de features; se hace en las tareas siguientes. Solo commitea layout + rutas si compila tras crear login/dashboard; si no, commitea junto con Task 7.5/7.6).

```bash
git add frontend/src/app/layout frontend/src/app/app.component.ts frontend/src/app/app.routes.ts
git commit -m "feat(frontend): shell con navegación y rutas protegidas por guard"
```

---

## Task 7.5: Pantalla de Login

**Files:**
- Create: `frontend/src/app/features/auth/login.component.ts`

- [ ] **Step 1: `login.component.ts`**

```ts
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-espresso">
      <div class="bg-crema rounded-2xl shadow-2xl p-8 w-full max-w-sm">
        <h1 class="font-display text-2xl mb-1">☕ Trazabilidad de Café</h1>
        <p class="text-sm text-espresso/70 mb-6">Inicia sesión para continuar</p>
        <form [formGroup]="form" (ngSubmit)="enviar()" class="space-y-4">
          <div>
            <label class="block text-sm mb-1">Usuario</label>
            <input formControlName="username" class="w-full px-3 py-2 rounded border border-espresso/20 bg-white" />
          </div>
          <div>
            <label class="block text-sm mb-1">Contraseña</label>
            <input type="password" formControlName="password" class="w-full px-3 py-2 rounded border border-espresso/20 bg-white" />
          </div>
          @if (error()) { <p class="text-sm text-red-600">{{ error() }}</p> }
          <button type="submit" [disabled]="form.invalid || cargando()"
                  class="w-full py-2 rounded bg-ambar text-white font-semibold disabled:opacity-50">
            {{ cargando() ? 'Entrando…' : 'Entrar' }}
          </button>
        </form>
        <p class="text-xs text-espresso/50 mt-4">Demo: admin/admin123 · user/user123</p>
      </div>
    </div>`,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  cargando = signal(false);
  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  enviar() {
    if (this.form.invalid) return;
    this.cargando.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => { this.error.set('Credenciales inválidas'); this.cargando.set(false); },
    });
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/features/auth
git commit -m "feat(frontend): pantalla de login con reactive forms"
```

---

## Task 7.6: Dashboard

**Files:**
- Create: `frontend/src/app/features/dashboard/dashboard.service.ts`
- Create: `frontend/src/app/features/dashboard/dashboard.component.ts`

- [ ] **Step 1: `dashboard.service.ts`**

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Resumen } from '../../core/models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  resumen(): Observable<Resumen> {
    return this.http.get<Resumen>(`${environment.apiUrl}/dashboard/resumen`);
  }
}
```

- [ ] **Step 2: `dashboard.component.ts`**

```ts
import { Component, inject, signal, OnInit } from '@angular/core';
import { DashboardService } from './dashboard.service';
import { Resumen } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <h1 class="font-display text-2xl mb-6">Resumen</h1>
    @if (resumen(); as r) {
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-xl p-5 shadow-sm border border-espresso/5">
          <div class="text-3xl font-display text-tostado">{{ r.lotesVerdesDisponibles }}</div>
          <div class="text-sm text-espresso/70 mt-1">Lotes verdes disponibles</div>
        </div>
        <div class="bg-white rounded-xl p-5 shadow-sm border border-espresso/5">
          <div class="text-3xl font-display text-tostado">{{ r.lotesTostadosRegistrados }}</div>
          <div class="text-sm text-espresso/70 mt-1">Lotes tostados registrados</div>
        </div>
        <div class="bg-white rounded-xl p-5 shadow-sm border border-espresso/5">
          <div class="text-3xl font-display text-ambar">{{ r.mermaMediaPorcentaje }}%</div>
          <div class="text-sm text-espresso/70 mt-1">Merma media</div>
        </div>
        <div class="bg-white rounded-xl p-5 shadow-sm border border-espresso/5">
          <div class="text-3xl font-display text-tostado">{{ r.totalFincas }}</div>
          <div class="text-sm text-espresso/70 mt-1">Fincas registradas</div>
        </div>
      </div>
    } @else {
      <p class="text-espresso/60">Cargando…</p>
    }`,
})
export class DashboardComponent implements OnInit {
  private service = inject(DashboardService);
  resumen = signal<Resumen | null>(null);
  ngOnInit() { this.service.resumen().subscribe((r) => this.resumen.set(r)); }
}
```

- [ ] **Step 3: Build y commit**

Run: `cd frontend && npm run build`
Expected: build sin errores (login, dashboard, shell y rutas ya resuelven; los componentes de fincas/lotes se crean a continuación — si el build falla por imports de rutas aún inexistentes, continúa con Task 7.7 y commitea al final de esa tarea).

```bash
git add frontend/src/app/features/dashboard
git commit -m "feat(frontend): dashboard de resumen con tarjetas de métricas"
```

---

## Task 7.7: Mantenimiento de Fincas (CRUD)

**Files:**
- Create: `frontend/src/app/features/fincas/finca.service.ts`
- Create: `frontend/src/app/features/fincas/fincas.component.ts`

- [ ] **Step 1: `finca.service.ts`**

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud.service';
import { environment } from '../../../environments/environment';
import { Finca, FincaRequest } from '../../core/models/finca.model';

@Injectable({ providedIn: 'root' })
export class FincaService extends CrudService<Finca, FincaRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/fincas`);
  }
}
```

- [ ] **Step 2: `fincas.component.ts`**

```ts
import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FincaService } from './finca.service';
import { Finca, FincaRequest, Proceso } from '../../core/models/finca.model';
import { Page } from '../../core/models/page.model';
import { AuthService } from '../../core/services/auth.service';
import { PaginadorComponent } from '../../shared/components/paginador.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion.component';
import { BadgeComponent } from '../../shared/components/badge.component';

@Component({
  selector: 'app-fincas',
  standalone: true,
  imports: [ReactiveFormsModule, PaginadorComponent, ModalConfirmacionComponent, BadgeComponent],
  template: `
    <div class="flex items-center justify-between mb-6">
      <h1 class="font-display text-2xl">Fincas</h1>
      @if (auth.isAdmin()) {
        <button class="px-4 py-2 rounded bg-ambar text-white text-sm font-semibold" (click)="nuevo()">+ Nueva finca</button>
      }
    </div>

    <div class="flex gap-2 mb-4">
      <input [value]="q()" (input)="buscar($any($event.target).value)"
             placeholder="Buscar por nombre o región…"
             class="px-3 py-2 rounded border border-espresso/20 bg-white w-72 text-sm" />
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-espresso/5 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-espresso/5 text-left">
          <tr>
            <th class="px-4 py-3">Nombre</th><th class="px-4 py-3">País / Región</th>
            <th class="px-4 py-3">Altitud</th><th class="px-4 py-3">Variedad</th>
            <th class="px-4 py-3">Proceso</th><th class="px-4 py-3 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (f of page()?.content ?? []; track f.id) {
            <tr class="border-t border-espresso/5">
              <td class="px-4 py-3 font-medium">{{ f.nombre }}</td>
              <td class="px-4 py-3">{{ f.pais }} · {{ f.region }}</td>
              <td class="px-4 py-3">{{ f.altitudMsnm }} msnm</td>
              <td class="px-4 py-3">{{ f.variedad }}</td>
              <td class="px-4 py-3"><app-badge [texto]="f.proceso" tipo="ambar" /></td>
              <td class="px-4 py-3 text-right space-x-2">
                @if (auth.isAdmin()) {
                  <button class="text-tostado hover:underline" (click)="editar(f)">Editar</button>
                  <button class="text-red-600 hover:underline" (click)="pedirBorrado(f)">Eliminar</button>
                } @else { <span class="text-espresso/40">—</span> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="px-4 py-8 text-center text-espresso/50">Sin resultados</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      <app-paginador [page]="p.page" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
                     [first]="p.first" [last]="p.last" (cambiar)="irPagina($event)" />
    }

    @if (panelAbierto()) {
      <div class="fixed inset-0 bg-espresso/30 flex justify-end z-40" (click)="cerrar()">
        <div class="bg-white w-full max-w-md h-full p-6 overflow-auto" (click)="$event.stopPropagation()">
          <h2 class="font-display text-xl mb-4">{{ editando() ? 'Editar finca' : 'Nueva finca' }}</h2>
          <form [formGroup]="form" (ngSubmit)="guardar()" class="space-y-3 text-sm">
            <div><label class="block mb-1">Nombre*</label>
              <input formControlName="nombre" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            <div class="grid grid-cols-2 gap-3">
              <div><label class="block mb-1">País*</label>
                <input formControlName="pais" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
              <div><label class="block mb-1">Región*</label>
                <input formControlName="region" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            </div>
            <div><label class="block mb-1">Productor</label>
              <input formControlName="productor" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            <div class="grid grid-cols-2 gap-3">
              <div><label class="block mb-1">Altitud (msnm)</label>
                <input type="number" formControlName="altitudMsnm" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
              <div><label class="block mb-1">Variedad</label>
                <input formControlName="variedad" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            </div>
            <div><label class="block mb-1">Proceso*</label>
              <select formControlName="proceso" class="w-full px-3 py-2 rounded border border-espresso/20">
                <option value="LAVADO">Lavado</option>
                <option value="NATURAL">Natural</option>
                <option value="HONEY">Honey</option>
              </select></div>
            @if (errorApi()) { <p class="text-red-600">{{ errorApi() }}</p> }
            <div class="flex justify-end gap-2 pt-3">
              <button type="button" class="px-4 py-2 rounded border border-espresso/20" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid" class="px-4 py-2 rounded bg-ambar text-white disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <app-modal-confirmacion [abierto]="!!aBorrar()" titulo="Eliminar finca"
      [mensaje]="'¿Eliminar la finca ' + (aBorrar()?.nombre ?? '') + '?'"
      (confirmar)="confirmarBorrado()" (cancelar)="aBorrar.set(null)" />
  `,
})
export class FincasComponent implements OnInit {
  private service = inject(FincaService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<Finca> | null>(null);
  q = signal('');
  panelAbierto = signal(false);
  editando = signal<Finca | null>(null);
  aBorrar = signal<Finca | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    pais: ['', Validators.required],
    region: ['', Validators.required],
    productor: [''],
    altitudMsnm: [null as number | null],
    variedad: [''],
    proceso: ['LAVADO' as Proceso, Validators.required],
  });

  ngOnInit() { this.cargar(); }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q() }).subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  nuevo() { this.editando.set(null); this.form.reset({ proceso: 'LAVADO' }); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(f: Finca) { this.editando.set(f); this.form.patchValue(f); this.errorApi.set(null); this.panelAbierto.set(true); }
  cerrar() { this.panelAbierto.set(false); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as FincaRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  pedirBorrado(f: Finca) { this.aBorrar.set(f); }
  confirmarBorrado() {
    const f = this.aBorrar(); if (!f) return;
    this.service.delete(f.id).subscribe({
      next: () => { this.aBorrar.set(null); this.cargar(); },
      error: (e) => { this.aBorrar.set(null); alert(e?.error?.message ?? 'No se pudo eliminar'); },
    });
  }
}
```

- [ ] **Step 3: Build y commit**

Run: `cd frontend && npm run build`
Expected: build sin errores.

```bash
git add frontend/src/app/features/fincas
git commit -m "feat(frontend): mantenimiento CRUD de fincas con panel y borrado confirmado"
```

---

## Task 7.8: Mantenimiento de Lotes Verdes (CRUD)

**Files:**
- Create: `frontend/src/app/features/lotes-verdes/lote-verde.service.ts`
- Create: `frontend/src/app/features/lotes-verdes/lotes-verdes.component.ts`

> Patrón idéntico al de fincas (tabla + panel lateral + borrado confirmado). Cambian columnas,
> campos del formulario (incluye selector de finca) y el filtro por estado.

- [ ] **Step 1: `lote-verde.service.ts`**

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud.service';
import { environment } from '../../../environments/environment';
import { LoteVerde, LoteVerdeRequest } from '../../core/models/lote-verde.model';

@Injectable({ providedIn: 'root' })
export class LoteVerdeService extends CrudService<LoteVerde, LoteVerdeRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/lotes-verdes`);
  }
}
```

- [ ] **Step 2: `lotes-verdes.component.ts`**

```ts
import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteVerdeService } from './lote-verde.service';
import { FincaService } from '../fincas/finca.service';
import { LoteVerde, LoteVerdeRequest } from '../../core/models/lote-verde.model';
import { Finca } from '../../core/models/finca.model';
import { Page } from '../../core/models/page.model';
import { AuthService } from '../../core/services/auth.service';
import { PaginadorComponent } from '../../shared/components/paginador.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion.component';
import { BadgeComponent } from '../../shared/components/badge.component';

@Component({
  selector: 'app-lotes-verdes',
  standalone: true,
  imports: [ReactiveFormsModule, PaginadorComponent, ModalConfirmacionComponent, BadgeComponent],
  template: `
    <div class="flex items-center justify-between mb-6">
      <h1 class="font-display text-2xl">Lotes de café verde</h1>
      @if (auth.isAdmin()) {
        <button class="px-4 py-2 rounded bg-ambar text-white text-sm font-semibold" (click)="nuevo()">+ Nuevo lote</button>
      }
    </div>

    <div class="flex gap-2 mb-4">
      <input [value]="q()" (input)="buscar($any($event.target).value)" placeholder="Buscar por código…"
             class="px-3 py-2 rounded border border-espresso/20 bg-white w-64 text-sm" />
      <select [value]="estado()" (change)="filtrarEstado($any($event.target).value)"
              class="px-3 py-2 rounded border border-espresso/20 bg-white text-sm">
        <option value="">Todos los estados</option>
        <option value="DISPONIBLE">Disponible</option>
        <option value="AGOTADO">Agotado</option>
      </select>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-espresso/5 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-espresso/5 text-left">
          <tr>
            <th class="px-4 py-3">Código</th><th class="px-4 py-3">Finca</th>
            <th class="px-4 py-3">Peso (kg)</th><th class="px-4 py-3">SCA</th>
            <th class="px-4 py-3">Recepción</th><th class="px-4 py-3">Estado</th>
            <th class="px-4 py-3 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (l of page()?.content ?? []; track l.id) {
            <tr class="border-t border-espresso/5">
              <td class="px-4 py-3 font-medium">{{ l.codigo }}</td>
              <td class="px-4 py-3">{{ l.fincaNombre }}</td>
              <td class="px-4 py-3">{{ l.pesoKg }}</td>
              <td class="px-4 py-3">{{ l.puntajeSca }}</td>
              <td class="px-4 py-3">{{ l.fechaRecepcion }}</td>
              <td class="px-4 py-3">
                <app-badge [texto]="l.estado" [tipo]="l.estado === 'DISPONIBLE' ? 'verde' : 'gris'" />
              </td>
              <td class="px-4 py-3 text-right space-x-2">
                @if (auth.isAdmin()) {
                  <button class="text-tostado hover:underline" (click)="editar(l)">Editar</button>
                  <button class="text-red-600 hover:underline" (click)="aBorrar.set(l)">Eliminar</button>
                } @else { <span class="text-espresso/40">—</span> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="px-4 py-8 text-center text-espresso/50">Sin resultados</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      <app-paginador [page]="p.page" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
                     [first]="p.first" [last]="p.last" (cambiar)="irPagina($event)" />
    }

    @if (panelAbierto()) {
      <div class="fixed inset-0 bg-espresso/30 flex justify-end z-40" (click)="cerrar()">
        <div class="bg-white w-full max-w-md h-full p-6 overflow-auto" (click)="$event.stopPropagation()">
          <h2 class="font-display text-xl mb-4">{{ editando() ? 'Editar lote verde' : 'Nuevo lote verde' }}</h2>
          <form [formGroup]="form" (ngSubmit)="guardar()" class="space-y-3 text-sm">
            <div><label class="block mb-1">Código*</label>
              <input formControlName="codigo" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            <div><label class="block mb-1">Finca*</label>
              <select formControlName="fincaId" class="w-full px-3 py-2 rounded border border-espresso/20">
                <option [ngValue]="null" disabled>Selecciona…</option>
                @for (f of fincas(); track f.id) { <option [ngValue]="f.id">{{ f.nombre }}</option> }
              </select></div>
            <div class="grid grid-cols-2 gap-3">
              <div><label class="block mb-1">Peso (kg)*</label>
                <input type="number" step="0.01" formControlName="pesoKg" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
              <div><label class="block mb-1">Humedad (%)</label>
                <input type="number" step="0.1" formControlName="humedadPorcentaje" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div><label class="block mb-1">Puntaje SCA</label>
                <input type="number" step="0.1" formControlName="puntajeSca" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
              <div><label class="block mb-1">Fecha recepción*</label>
                <input type="date" formControlName="fechaRecepcion" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            </div>
            @if (errorApi()) { <p class="text-red-600">{{ errorApi() }}</p> }
            <div class="flex justify-end gap-2 pt-3">
              <button type="button" class="px-4 py-2 rounded border border-espresso/20" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid" class="px-4 py-2 rounded bg-ambar text-white disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <app-modal-confirmacion [abierto]="!!aBorrar()" titulo="Eliminar lote verde"
      [mensaje]="'¿Eliminar el lote ' + (aBorrar()?.codigo ?? '') + '?'"
      (confirmar)="confirmarBorrado()" (cancelar)="aBorrar.set(null)" />
  `,
})
export class LotesVerdesComponent implements OnInit {
  private service = inject(LoteVerdeService);
  private fincaService = inject(FincaService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<LoteVerde> | null>(null);
  fincas = signal<Finca[]>([]);
  q = signal('');
  estado = signal('');
  panelAbierto = signal(false);
  editando = signal<LoteVerde | null>(null);
  aBorrar = signal<LoteVerde | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    fincaId: [null as number | null, Validators.required],
    pesoKg: [null as number | null, [Validators.required, Validators.min(0)]],
    humedadPorcentaje: [null as number | null],
    puntajeSca: [null as number | null],
    fechaRecepcion: ['', Validators.required],
  });

  ngOnInit() {
    this.cargar();
    this.fincaService.list({ size: 100 }).subscribe((p) => this.fincas.set(p.content));
  }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q(), estado: this.estado() || undefined })
      .subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  filtrarEstado(v: string) { this.estado.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  nuevo() { this.editando.set(null); this.form.reset(); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(l: LoteVerde) {
    this.editando.set(l);
    this.form.patchValue({
      codigo: l.codigo, fincaId: l.fincaId, pesoKg: l.pesoKg,
      humedadPorcentaje: l.humedadPorcentaje ?? null, puntajeSca: l.puntajeSca ?? null,
      fechaRecepcion: l.fechaRecepcion,
    });
    this.errorApi.set(null);
    this.panelAbierto.set(true);
  }
  cerrar() { this.panelAbierto.set(false); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as LoteVerdeRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  confirmarBorrado() {
    const l = this.aBorrar(); if (!l) return;
    this.service.delete(l.id).subscribe({
      next: () => { this.aBorrar.set(null); this.cargar(); },
      error: (e) => { this.aBorrar.set(null); alert(e?.error?.message ?? 'No se pudo eliminar'); },
    });
  }
}
```

> Nota: el `<option [ngValue]="null">` requiere importar `ReactiveFormsModule` (ya incluido).
> Para `[ngValue]` no hace falta `CommonModule` adicional con el bloque `@for`.

- [ ] **Step 3: Build y commit**

Run: `cd frontend && npm run build`
Expected: build sin errores.

```bash
git add frontend/src/app/features/lotes-verdes
git commit -m "feat(frontend): mantenimiento CRUD de lotes verdes con filtro por estado"
```

---

## Task 7.9: Mantenimiento de Lotes Tostados (CRUD principal)

**Files:**
- Create: `frontend/src/app/features/lotes-tostados/lote-tostado.service.ts`
- Create: `frontend/src/app/features/lotes-tostados/lotes-tostados.component.ts`

> CRUD principal. El alta muestra la merma calculada (solo informativa; el backend la recalcula).
> El borrado es una **anulación** (DELETE lógico) que devuelve stock al lote verde.

- [ ] **Step 1: `lote-tostado.service.ts`**

```ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud.service';
import { environment } from '../../../environments/environment';
import { LoteTostado, LoteTostadoRequest } from '../../core/models/lote-tostado.model';

@Injectable({ providedIn: 'root' })
export class LoteTostadoService extends CrudService<LoteTostado, LoteTostadoRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/lotes-tostados`);
  }
}
```

- [ ] **Step 2: `lotes-tostados.component.ts`**

```ts
import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteTostadoService } from './lote-tostado.service';
import { LoteVerdeService } from '../lotes-verdes/lote-verde.service';
import { LoteTostado, LoteTostadoRequest, PerfilTueste } from '../../core/models/lote-tostado.model';
import { LoteVerde } from '../../core/models/lote-verde.model';
import { Page } from '../../core/models/page.model';
import { AuthService } from '../../core/services/auth.service';
import { PaginadorComponent } from '../../shared/components/paginador.component';
import { ModalConfirmacionComponent } from '../../shared/components/modal-confirmacion.component';
import { BadgeComponent } from '../../shared/components/badge.component';

@Component({
  selector: 'app-lotes-tostados',
  standalone: true,
  imports: [ReactiveFormsModule, PaginadorComponent, ModalConfirmacionComponent, BadgeComponent],
  template: `
    <div class="flex items-center justify-between mb-6">
      <h1 class="font-display text-2xl">Lotes tostados</h1>
      @if (auth.isAdmin()) {
        <button class="px-4 py-2 rounded bg-ambar text-white text-sm font-semibold" (click)="nuevo()">+ Nuevo tueste</button>
      }
    </div>

    <div class="flex gap-2 mb-4">
      <input [value]="q()" (input)="buscar($any($event.target).value)" placeholder="Buscar por código…"
             class="px-3 py-2 rounded border border-espresso/20 bg-white w-64 text-sm" />
      <select [value]="perfil()" (change)="filtrarPerfil($any($event.target).value)"
              class="px-3 py-2 rounded border border-espresso/20 bg-white text-sm">
        <option value="">Todos los perfiles</option>
        <option value="LIGHT">Light</option><option value="MEDIUM">Medium</option><option value="DARK">Dark</option>
      </select>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-espresso/5 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-espresso/5 text-left">
          <tr>
            <th class="px-4 py-3">Código</th><th class="px-4 py-3">Lote verde</th>
            <th class="px-4 py-3">Perfil</th><th class="px-4 py-3">Entrada/Salida</th>
            <th class="px-4 py-3">Merma</th><th class="px-4 py-3">Estado</th>
            <th class="px-4 py-3 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (t of page()?.content ?? []; track t.id) {
            <tr class="border-t border-espresso/5" [class.opacity-50]="t.estado === 'ANULADO'">
              <td class="px-4 py-3 font-medium">{{ t.codigo }}</td>
              <td class="px-4 py-3">{{ t.loteVerdeCodigo }}</td>
              <td class="px-4 py-3"><app-badge [texto]="t.perfilTueste" [tipo]="perfilColor(t.perfilTueste)" /></td>
              <td class="px-4 py-3">{{ t.pesoEntradaKg }} → {{ t.pesoSalidaKg }} kg</td>
              <td class="px-4 py-3 font-semibold text-ambar">{{ t.mermaPorcentaje }}%</td>
              <td class="px-4 py-3">
                <app-badge [texto]="t.estado" [tipo]="t.estado === 'REGISTRADO' ? 'verde' : 'rojo'" />
              </td>
              <td class="px-4 py-3 text-right space-x-2">
                @if (auth.isAdmin() && t.estado === 'REGISTRADO') {
                  <button class="text-tostado hover:underline" (click)="editar(t)">Editar</button>
                  <button class="text-red-600 hover:underline" (click)="aAnular.set(t)">Anular</button>
                } @else { <span class="text-espresso/40">—</span> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="px-4 py-8 text-center text-espresso/50">Sin resultados</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      <app-paginador [page]="p.page" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
                     [first]="p.first" [last]="p.last" (cambiar)="irPagina($event)" />
    }

    @if (panelAbierto()) {
      <div class="fixed inset-0 bg-espresso/30 flex justify-end z-40" (click)="cerrar()">
        <div class="bg-white w-full max-w-md h-full p-6 overflow-auto" (click)="$event.stopPropagation()">
          <h2 class="font-display text-xl mb-4">{{ editando() ? 'Editar tueste' : 'Nuevo tueste' }}</h2>
          <form [formGroup]="form" (ngSubmit)="guardar()" class="space-y-3 text-sm">
            <div><label class="block mb-1">Código*</label>
              <input formControlName="codigo" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            <div><label class="block mb-1">Lote verde*</label>
              <select formControlName="loteVerdeId" class="w-full px-3 py-2 rounded border border-espresso/20" [class.opacity-60]="!!editando()">
                <option [ngValue]="null" disabled>Selecciona…</option>
                @for (l of lotesVerdes(); track l.id) { <option [ngValue]="l.id">{{ l.codigo }} ({{ l.pesoKg }} kg)</option> }
              </select>
              @if (editando()) { <p class="text-xs text-espresso/50 mt-1">El lote verde no se cambia al editar.</p> }
            </div>
            <div><label class="block mb-1">Perfil de tueste*</label>
              <select formControlName="perfilTueste" class="w-full px-3 py-2 rounded border border-espresso/20">
                <option value="LIGHT">Light</option><option value="MEDIUM">Medium</option><option value="DARK">Dark</option>
              </select></div>
            <div class="grid grid-cols-2 gap-3">
              <div><label class="block mb-1">Peso entrada (kg)*</label>
                <input type="number" step="0.01" formControlName="pesoEntradaKg" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
              <div><label class="block mb-1">Peso salida (kg)*</label>
                <input type="number" step="0.01" formControlName="pesoSalidaKg" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            </div>
            <div class="bg-crema rounded p-3 text-sm">Merma estimada: <b>{{ mermaEstimada() }}%</b></div>
            <div><label class="block mb-1">Fecha de tueste*</label>
              <input type="datetime-local" formControlName="fechaTueste" class="w-full px-3 py-2 rounded border border-espresso/20" /></div>
            @if (errorApi()) { <p class="text-red-600">{{ errorApi() }}</p> }
            <div class="flex justify-end gap-2 pt-3">
              <button type="button" class="px-4 py-2 rounded border border-espresso/20" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid" class="px-4 py-2 rounded bg-ambar text-white disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <app-modal-confirmacion [abierto]="!!aAnular()" titulo="Anular tueste"
      [mensaje]="'¿Anular el lote ' + (aAnular()?.codigo ?? '') + '? Se devolverá el stock al lote verde.'"
      (confirmar)="confirmarAnular()" (cancelar)="aAnular.set(null)" />
  `,
})
export class LotesTostadosComponent implements OnInit {
  private service = inject(LoteTostadoService);
  private verdeService = inject(LoteVerdeService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<LoteTostado> | null>(null);
  lotesVerdes = signal<LoteVerde[]>([]);
  q = signal('');
  perfil = signal('');
  panelAbierto = signal(false);
  editando = signal<LoteTostado | null>(null);
  aAnular = signal<LoteTostado | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    loteVerdeId: [null as number | null, Validators.required],
    perfilTueste: ['MEDIUM' as PerfilTueste, Validators.required],
    pesoEntradaKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    pesoSalidaKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    fechaTueste: ['', Validators.required],
  });

  mermaEstimada = computed(() => {
    const e = Number(this.form.controls.pesoEntradaKg.value);
    const s = Number(this.form.controls.pesoSalidaKg.value);
    if (!e || !s || s >= e) return '0.00';
    return (((e - s) / e) * 100).toFixed(2);
  });

  ngOnInit() {
    this.cargar();
    this.verdeService.list({ size: 100, estado: 'DISPONIBLE' }).subscribe((p) => this.lotesVerdes.set(p.content));
  }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q(), perfil: this.perfil() || undefined })
      .subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  filtrarPerfil(v: string) { this.perfil.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  perfilColor(p: PerfilTueste): 'verde' | 'ambar' | 'gris' {
    return p === 'LIGHT' ? 'verde' : p === 'MEDIUM' ? 'ambar' : 'gris';
  }

  nuevo() { this.editando.set(null); this.form.reset({ perfilTueste: 'MEDIUM' }); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(t: LoteTostado) {
    this.editando.set(t);
    this.form.patchValue({
      codigo: t.codigo, loteVerdeId: t.loteVerdeId, perfilTueste: t.perfilTueste,
      pesoEntradaKg: t.pesoEntradaKg, pesoSalidaKg: t.pesoSalidaKg,
      fechaTueste: t.fechaTueste.substring(0, 16),
    });
    this.form.controls.loteVerdeId.disable();
    this.errorApi.set(null);
    this.panelAbierto.set(true);
  }
  cerrar() { this.panelAbierto.set(false); this.form.controls.loteVerdeId.enable(); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as LoteTostadoRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  confirmarAnular() {
    const t = this.aAnular(); if (!t) return;
    this.service.delete(t.id).subscribe({
      next: () => { this.aAnular.set(null); this.cargar(); },
      error: (e) => { this.aAnular.set(null); alert(e?.error?.message ?? 'No se pudo anular'); },
    });
  }
}
```

- [ ] **Step 3: Build, arranque integral y commit**

Run: `cd frontend && npm run build`
Expected: build sin errores.
Verificación end-to-end (con backend en `:8080` y `npm start` en `:4200`): login como admin, crear finca → lote verde → tueste, ver merma y descuento de stock, anular y comprobar devolución, y verificar que el usuario `user` no ve botones de escritura.

```bash
git add frontend/src/app/features/lotes-tostados
git commit -m "feat(frontend): mantenimiento CRUD de lotes tostados con merma y anulación"
```

---

# FASE 8 — README y cierre

## Task 8.1: README del proyecto

**Files:**
- Create: `README.md` (raíz)

- [ ] **Step 1: Escribir `README.md`**

```markdown
# ☕ Trazabilidad de Café de Especialidad — CRUD

Mantenimiento full-stack de la trazabilidad del café "de la finca a la taza":
**Finca → Lote de café verde → Lote tostado**, con autenticación JWT por roles
y documentación OpenAPI.

## Stack
- **Frontend:** Angular 18 + Tailwind CSS
- **Backend:** Java 21 + Spring Boot 3.3 (capas limpias) + Spring Security (JWT)
- **BD:** PostgreSQL 16 + Flyway
- **Docs API:** Swagger UI

## Requisitos
- Java 21, Maven 3.9+
- Node 20+ y npm
- PostgreSQL 16 en `localhost:5432` (configurable con `DB_PORT`)

## Puesta en marcha

### 1. Base de datos
```sql
CREATE DATABASE cafe_trazabilidad;
CREATE USER cafe WITH PASSWORD 'cafe';
GRANT ALL PRIVILEGES ON DATABASE cafe_trazabilidad TO cafe;
```
(O ajusta credenciales en `backend/src/main/resources/application.yml`.)

> **Puerto:** por defecto el backend usa el 5432. Si tu PostgreSQL escucha en otro puerto,
> arráncalo con `DB_PORT`, p. ej. `DB_PORT=5433 mvn spring-boot:run`.

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
- API en `http://localhost:8080`
- Swagger UI en `http://localhost:8080/swagger-ui.html`
- Flyway crea el esquema y carga datos demo automáticamente.

### 3. Frontend
```bash
cd frontend
npm install
npm start
```
- App en `http://localhost:4200` (proxy `/api` → `:8080`).

## Credenciales demo
| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| admin | admin123 | ADMIN | CRUD completo |
| user | user123 | USER | Solo lectura |

## Reglas de negocio destacadas
- **Merma de tueste:** `(entrada − salida) / entrada × 100`, calculada en el backend.
- **Control de stock:** al tostar se descuenta el peso del lote verde; al agotarse pasa a `AGOTADO`.
- **Anulación:** un tueste anulado devuelve el stock al lote verde.
- **Integridad:** no se borran fincas/lotes con dependencias.

## Tests
```bash
cd backend && mvn test
```

## Estructura
- `backend/` — API Spring Boot
- `frontend/` — SPA Angular
- `docs/superpowers/` — spec de diseño y plan de implementación
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: README con instrucciones de puesta en marcha y reglas de negocio"
```

---

## Resumen de cobertura del plan

| Requisito de la spec | Tareas |
|---|---|
| CRUD Finca | 3.1, 3.2, 3.3, 7.7 |
| CRUD LoteCafeVerde | 4.1, 4.2, 7.8 |
| CRUD LoteTostado (reglas negocio) | 5.1, 5.2, 5.3, 7.9 |
| Spring Security + JWT por roles | 2.1, 2.2, 2.3, 7.2 (interceptor/guards) |
| Swagger/OpenAPI | 6.2 |
| Flyway (esquema + seed) | 0.2 |
| Dashboard/resumen | 6.1, 7.6 |
| Manejo global de errores y validación | 1.1, DTOs por feature |
| Tests de negocio | 2.2, 3.2, 5.2 |
| README | 8.1 |
| UI Angular + Tailwind distintiva | 7.1–7.9 |

> **Orden recomendado de ejecución** para evitar stubs cruzados:
> Fase 0 → 1 → 2 → 4.1 (entidad LoteVerde) → 3 (Finca) → 5.1 → 4.2 → 5.2 → 5.3 → 6 → 7 → 8.
> En la práctica, ejecutar las fases en orden numérico funciona si en 3.2 y 4.2 se crean los
> repositorios mínimos indicados en sus notas.

