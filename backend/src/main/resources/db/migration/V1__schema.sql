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
