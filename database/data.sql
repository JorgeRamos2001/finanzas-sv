-- =============================================================================
--  FinanzasSV - Modulo de Contabilidad Automatizado
--  Script DML: datos iniciales (PostgreSQL)
--  Rama: feat/esquema-base-datos
-- =============================================================================
--  Contenido:
--    1. Usuarios por defecto (contrasenas cifradas con BCrypt)
--    2. Catalogo de Cuentas basico (niveles 1, 2 y 3)
--
--  Clasificacion contable por PRIMER DIGITO del codigo:
--    1 = Activo           (naturaleza DEUDOR)
--    2 = Pasivo           (naturaleza ACREEDOR)
--    3 = Capital Contable (naturaleza ACREEDOR)
--    4 = Costos y Gastos  (naturaleza DEUDOR)
--    5 = Ingresos         (naturaleza ACREEDOR)
--
--  Regla de negocio:
--    * Niveles 1 y 2  : cuentas PRINCIPALES (control). No aceptan movimientos.
--    * Nivel 3 (hojas): cuentas SECUNDARIAS (movimiento). Reciben los montos
--      parciales de cada asiento; los totales se consolidan hacia arriba.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. USUARIOS POR DEFECTO
--    admin    / admin123     -> ADMIN
--    contador / contador123  -> CONTADOR
--    consulta / consulta123  -> CONSULTA
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (id, username, password, nombre_completo, rol, activo) VALUES
    (1, 'admin',    '$2b$10$2tgCM3U9/Dl204doKT76Fu/5Zy18joj2FLo85Gbkoyjbeab9AoJO6', 'Administrador del Sistema', 'ADMIN',    TRUE),
    (2, 'contador', '$2b$10$.qmj4AE41k0u9a9qgf0YSu44n5tG7KYBqzgK20epEByn8W7l4R4Ry', 'Contador General',          'CONTADOR', TRUE),
    (3, 'consulta', '$2b$10$RHton803cKpl6rKXU.FVdOms7hHFM1rCfCvz9haKV2FVOEGeMrCda', 'Auditor / Consulta',        'CONSULTA', TRUE);
SELECT setval('usuarios_id_seq', 3, TRUE);

-- -----------------------------------------------------------------------------
-- 2. CATALOGO DE CUENTAS - NIVEL 1 (cuentas principales de agrupacion)
-- -----------------------------------------------------------------------------
INSERT INTO cuentas (id, codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos) VALUES
    (1, '1', 'Activo',             1, NULL, 'DEUDOR',   FALSE),
    (2, '2', 'Pasivo',             1, NULL, 'ACREEDOR', FALSE),
    (3, '3', 'Capital Contable',   1, NULL, 'ACREEDOR', FALSE),
    (4, '4', 'Costos y Gastos',    1, NULL, 'DEUDOR',   FALSE),
    (5, '5', 'Ingresos',           1, NULL, 'ACREEDOR', FALSE);

-- -----------------------------------------------------------------------------
-- 3. CATALOGO DE CUENTAS - NIVEL 2 (cuentas principales de control)
-- -----------------------------------------------------------------------------
INSERT INTO cuentas (id, codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos) VALUES
    -- Activo
    (6,  '11', 'Efectivo y Equivalentes',      2, 1, 'DEUDOR',   FALSE),
    (7,  '12', 'Cuentas por Cobrar',           2, 1, 'DEUDOR',   FALSE),
    (8,  '13', 'Inventarios',                  2, 1, 'DEUDOR',   FALSE),
    (9,  '14', 'Propiedad, Planta y Equipo',   2, 1, 'DEUDOR',   FALSE),
    -- Pasivo
    (10, '21', 'Cuentas por Pagar',            2, 2, 'ACREEDOR', FALSE),
    (11, '22', 'Prestamos por Pagar',          2, 2, 'ACREEDOR', FALSE),
    -- Capital Contable
    (12, '31', 'Capital Social',               2, 3, 'ACREEDOR', FALSE),
    (13, '32', 'Utilidades Retenidas',         2, 3, 'ACREEDOR', FALSE),
    -- Costos y Gastos
    (14, '41', 'Costo de Ventas',              2, 4, 'DEUDOR',   FALSE),
    (15, '42', 'Gastos Operativos',            2, 4, 'DEUDOR',   FALSE),
    -- Ingresos
    (16, '51', 'Ventas',                       2, 5, 'ACREEDOR', FALSE),
    (17, '52', 'Otros Ingresos',               2, 5, 'ACREEDOR', FALSE);

-- -----------------------------------------------------------------------------
-- 4. CATALOGO DE CUENTAS - NIVEL 3 (cuentas secundarias de movimiento)
-- -----------------------------------------------------------------------------
INSERT INTO cuentas (id, codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos) VALUES
    -- Activo
    (18, '111', 'Caja',                   3, 6,  'DEUDOR',   TRUE),
    (19, '112', 'Bancos',                 3, 6,  'DEUDOR',   TRUE),
    (20, '121', 'Clientes',               3, 7,  'DEUDOR',   TRUE),
    (21, '131', 'Mercaderias',            3, 8,  'DEUDOR',   TRUE),
    (22, '141', 'Mobiliario y Equipo',    3, 9,  'DEUDOR',   TRUE),
    (23, '142', 'Equipo de Computo',      3, 9,  'DEUDOR',   TRUE),
    -- Pasivo
    (24, '211', 'Proveedores',            3, 10, 'ACREEDOR', TRUE),
    (25, '221', 'Prestamos Bancarios',    3, 11, 'ACREEDOR', TRUE),
    -- Capital Contable
    (26, '311', 'Capital Ordinario',      3, 12, 'ACREEDOR', TRUE),
    (27, '321', 'Utilidades del Ejercicio', 3, 13, 'ACREEDOR', TRUE),
    -- Costos y Gastos
    (28, '411', 'Costo de Mercaderias',   3, 14, 'DEUDOR',   TRUE),
    (29, '421', 'Sueldos y Salarios',     3, 15, 'DEUDOR',   TRUE),
    (30, '422', 'Renta / Alquileres',     3, 15, 'DEUDOR',   TRUE),
    (31, '423', 'Energia Electrica',      3, 15, 'DEUDOR',   TRUE),
    (32, '424', 'Papeleria y Utiles',     3, 15, 'DEUDOR',   TRUE),
    -- Ingresos
    (33, '511', 'Ventas Locales',         3, 16, 'ACREEDOR', TRUE),
    (34, '521', 'Ingresos por Intereses', 3, 17, 'ACREEDOR', TRUE);

SELECT setval('cuentas_id_seq', 34, TRUE);
