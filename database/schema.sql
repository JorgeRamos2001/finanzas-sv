-- =============================================================================
--  FinanzasSV - Modulo de Contabilidad Automatizado
--  Script DDL (PostgreSQL)
--  Rama: feat/esquema-base-datos
-- =============================================================================
--  Modelo de datos:
--    * usuarios          : credenciales y roles de acceso (ADMIN/CONTADOR/CONSULTA)
--    * cuentas           : catalogo contable jerarquico (arbol autoreferencial)
--    * asientos          : encabezado del Libro Diario
--    * asiento_detalles  : lineas Debe/Haber de cada asiento
--
--  Regla de negocio critica (Ley de la Partida Doble):
--    El total del Debe debe ser EXACTAMENTE igual al total del Haber.
--    Se garantiza con un CHECK a nivel de base de datos y se revalida en
--    backend (servicio) y frontend.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABLA: usuarios
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL,
    password            VARCHAR(100) NOT NULL,
    nombre_completo     VARCHAR(120) NOT NULL,
    rol                 VARCHAR(20)  NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    fecha_actualizacion TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'CONTADOR', 'CONSULTA'))
);

COMMENT ON TABLE usuarios IS 'Usuarios del sistema con roles de acceso';

-- -----------------------------------------------------------------------------
-- TABLA: cuentas (Catalogo contable jerarquico)
-- -----------------------------------------------------------------------------
--  Nivel 1-2      : cuentas principales (control), NO aceptan movimientos.
--  Nivel 3 (hoja) : cuentas secundarias (movimiento), aceptan movimientos.
--  Los saldos de las cuentas principales se consolidan automaticamente
--  (mayorizacion) a partir de los movimientos de sus cuentas secundarias.
-- -----------------------------------------------------------------------------
CREATE TABLE cuentas (
    id                 BIGSERIAL PRIMARY KEY,
    codigo             VARCHAR(20)  NOT NULL,
    nombre             VARCHAR(120) NOT NULL,
    nivel              INTEGER      NOT NULL,
    cuenta_padre_id    BIGINT,
    naturaleza         VARCHAR(10)  NOT NULL,
    acepta_movimientos BOOLEAN      NOT NULL DEFAULT FALSE,
    saldo_debe         NUMERIC(14,2) NOT NULL DEFAULT 0,
    saldo_haber        NUMERIC(14,2) NOT NULL DEFAULT 0,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_cuentas_codigo UNIQUE (codigo),
    CONSTRAINT fk_cuentas_padre FOREIGN KEY (cuenta_padre_id)
        REFERENCES cuentas (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_cuentas_nivel CHECK (nivel BETWEEN 1 AND 4),
    CONSTRAINT chk_cuentas_naturaleza CHECK (naturaleza IN ('DEUDOR', 'ACREEDOR')),
    CONSTRAINT chk_cuentas_saldos CHECK (saldo_debe >= 0 AND saldo_haber >= 0),
    CONSTRAINT uk_cuentas_nombre_por_padre UNIQUE (cuenta_padre_id, nombre)
);

COMMENT ON TABLE cuentas IS 'Catalogo contable jerarquico con consolidacion de saldos (Libro Mayor)';

CREATE INDEX idx_cuentas_padre ON cuentas (cuenta_padre_id);
CREATE INDEX idx_cuentas_nivel ON cuentas (nivel);
CREATE INDEX idx_cuentas_movimiento ON cuentas (acepta_movimientos) WHERE acepta_movimientos;

-- -----------------------------------------------------------------------------
-- TABLA: asientos (Libro Diario - encabezado)
-- -----------------------------------------------------------------------------
CREATE TABLE asientos (
    id             BIGSERIAL PRIMARY KEY,
    numero_asiento BIGINT        NOT NULL,
    fecha          DATE          NOT NULL,
    concepto       VARCHAR(255)  NOT NULL,
    usuario_id     BIGINT        NOT NULL,
    total_debe     NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_haber    NUMERIC(14,2) NOT NULL DEFAULT 0,
    estado         VARCHAR(15)   NOT NULL DEFAULT 'REGISTRADO',
    fecha_creacion TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uk_asientos_numero UNIQUE (numero_asiento),
    CONSTRAINT fk_asientos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    -- REGLA CRITICA: Ley de la Partida Doble a nivel de base de datos
    CONSTRAINT chk_asientos_partida_doble CHECK (total_debe = total_haber),
    CONSTRAINT chk_asientos_montos CHECK (total_debe > 0),
    CONSTRAINT chk_asientos_estado CHECK (estado IN ('REGISTRADO', 'ANULADO'))
);

COMMENT ON TABLE asientos IS 'Encabezado de asientos del Libro Diario';

CREATE INDEX idx_asientos_fecha ON asientos (fecha);
CREATE INDEX idx_asientos_usuario ON asientos (usuario_id);

-- -----------------------------------------------------------------------------
-- TABLA: asiento_detalles (Libro Diario - lineas Debe / Haber)
-- -----------------------------------------------------------------------------
CREATE TABLE asiento_detalles (
    id          BIGSERIAL PRIMARY KEY,
    asiento_id  BIGINT        NOT NULL,
    cuenta_id   BIGINT        NOT NULL,
    concepto    VARCHAR(255)  NOT NULL,
    monto_debe  NUMERIC(14,2) NOT NULL DEFAULT 0,
    monto_haber NUMERIC(14,2) NOT NULL DEFAULT 0,
    orden_linea INT           NOT NULL DEFAULT 0,

    CONSTRAINT fk_detalles_asiento FOREIGN KEY (asiento_id)
        REFERENCES asientos (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_detalles_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES cuentas (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    -- Cada linea mueve monto en EXACTAMENTE una de las dos columnas
    CONSTRAINT chk_detalles_linea_exclusiva CHECK (
        (monto_debe > 0 AND monto_haber = 0) OR (monto_haber > 0 AND monto_debe = 0)
    )
);

COMMENT ON TABLE asiento_detalles IS 'Lineas Debe/Haber de cada asiento del Libro Diario';

CREATE INDEX idx_detalles_asiento ON asiento_detalles (asiento_id);
CREATE INDEX idx_detalles_cuenta ON asiento_detalles (cuenta_id);
