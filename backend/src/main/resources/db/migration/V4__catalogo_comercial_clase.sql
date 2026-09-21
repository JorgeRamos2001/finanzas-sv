-- =============================================================================
--  FinanzasSV - V4: Catalogo comercial de la clase (Universidad Catolica de El Salvador)
--  Rama: feat/catalogo-comercial-clase
-- =============================================================================
--  Reemplaza el catalogo basico por el catalogo comercial de clase ADAPTADO:
--    * Codigos EXACTOS del manual de cuentas (sin puntos, como la clase)
--    * Nivel segun longitud del codigo (1 digito=1, 2=2, 4=3, 6=4, 8=5, 10=6)
--    * Cuentas de ultimo nivel (hoja) aceptan movimientos
--    * Cuentas (CR) (contrarias): naturaleza ACREEDOR
--    * EXCLUIDO: rubro 7 CUENTAS DE ORDEN (no participa en la partida doble
--      ni en los estados financieros del sistema)
--    * REINICIA los asientos de prueba: TRUNCATE con reinicio de secuencias
--
--  Clasificacion por PRIMER DIGITO (identica a la de la clase):
--    1 = ACTIVO                          (Activo = Pasivo + Capital)
--    2 = PASIVO
--    3 = PATRIMONIO
--    4 = CUENTAS DE RESULTADO DEUDORAS  (Costos y Gastos)
--    5 = CUENTAS DE RESULTADO ACREEDORAS (Ingresos)
--    6 = CUENTA DE CIERRE
-- =============================================================================

-- 1. Ampliar niveles de 1-4 a 1-6 (estructura de 1, 2, 4, 6, 8 y 10 digitos)
ALTER TABLE cuentas DROP CONSTRAINT chk_cuentas_nivel;
ALTER TABLE cuentas
    ADD CONSTRAINT chk_cuentas_nivel CHECK (nivel BETWEEN 1 AND 6);

-- 2. Reiniciar catalogo y asientos de prueba (los codigos cambian por completo)
TRUNCATE TABLE asiento_detalles, asientos, cuentas RESTART IDENTITY CASCADE;

-- =============================================================================
-- NIVEL 1 - RUBROS DE AGRUPACION (1 digito)
-- =============================================================================
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos) VALUES
    ('1', 'ACTIVO', 1, NULL, 'DEUDOR', FALSE),
    ('2', 'PASIVO', 1, NULL, 'ACREEDOR', FALSE),
    ('3', 'PATRIMONIO', 1, NULL, 'ACREEDOR', FALSE),
    ('4', 'CUENTAS DE RESULTADO DEUDORAS', 1, NULL, 'DEUDOR', FALSE),
    ('5', 'CUENTAS DE RESULTADO ACREEDORAS', 1, NULL, 'ACREEDOR', FALSE),
    ('6', 'CUENTA DE CIERRE', 1, NULL, 'ACREEDOR', FALSE);

-- =============================================================================
-- NIVEL 2 - RUBROS (2 digitos)
-- =============================================================================
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11', 'ACTIVO CORRIENTE', 2, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12', 'ACTIVO NO CORRIENTE', 2, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21', 'PASIVO CORRIENTE', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '2';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '22', 'PASIVO NO CORRIENTE', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '2';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '31', 'CAPITAL Y RESERVAS', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '3';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '32', 'RESULTADOS POR APLICAR', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '3';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41', 'COSTOS Y GASTOS DE OPERACIÓN', 2, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '42', 'OTROS COSTOS Y GASTOS', 2, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '51', 'INGRESOS POR VENTAS', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '5';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '52', 'OTROS PRODUCTOS', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '5';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '61', 'CUENTA LIQUIDADORA', 2, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '6';

-- =============================================================================
-- NIVEL 3 - CUENTAS DE MAYOR (4 digitos)
-- =============================================================================
-- ACTIVO CORRIENTE
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1101', 'EFECTIVO Y EQUIVALENTES DE EFECTIVO', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '11';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1102', 'CUENTAS Y DOCUMENTOS POR COBRAR', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '11';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1103', 'ESTIMACIÓN PARA CUENTAS INCOBRABLES (CR)', 3, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '11';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1105', 'INVENTARIOS', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '11';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1107', 'GASTOS PAGADOS POR ANTICIPADO', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '11';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1109', 'CRÉDITO FISCAL - IVA', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '11';
-- ACTIVO NO CORRIENTE
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1201', 'PROPIEDADES, PLANTA Y EQUIPO', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '12';
-- PASIVO CORRIENTE
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2101', 'PRÉSTAMOS A CORTO PLAZO Y SOBREGIROS', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2102', 'CUENTAS COMERCIALES POR PAGAR', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2103', 'ACREEDORES VARIOS', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2104', 'RETENCIONES POR PAGAR', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2105', 'BENEFICIOS A EMPLEADOS POR PAGAR', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2106', 'IMPUESTO SOBRE LA RENTA POR PAGAR', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2108', 'IVA - DÉBITO FISCAL', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '21';
-- PASIVO NO CORRIENTE
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '2201', 'PRÉSTAMOS POR PAGAR A LARGO PLAZO', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '22';
-- CAPITAL Y RESERVAS
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3101', 'CAPITAL SOCIAL', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '31';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3102', 'RESERVA LEGAL', 3, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '31';
-- RESULTADOS POR APLICAR
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3201', 'UTILIDADES DE EJERCICIOS ANTERIORES', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '32';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3202', 'UTILIDAD DEL PRESENTE EJERCICIO', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '32';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3203', 'DÉFICIT DE EJERCICIOS ANTERIORES', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '32';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '3204', 'DÉFICIT DEL PRESENTE EJERCICIO', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '32';
-- COSTOS Y GASTOS DE OPERACION
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '4101', 'COSTO DE VENTAS', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '41';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '4102', 'GASTOS ADMINISTRATIVOS', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '41';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '4103', 'GASTOS DE VENTA', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '41';
-- OTROS COSTOS Y GASTOS
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '4201', 'GASTOS FINANCIEROS', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '42';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '4202', 'PÉRDIDA EN VENTA O RETIRO DE ACTIVOS FIJOS', 3, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '42';
-- INGRESOS POR VENTAS
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '5101', 'INGRESOS OPERACIONALES', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '51';
-- OTROS PRODUCTOS
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '5201', 'PRODUCTOS FINANCIEROS', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '52';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '5202', 'GANANCIA EN VENTA DE ACTIVOS FIJOS', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '52';
-- CUENTA LIQUIDADORA
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '6101', 'PÉRDIDAS Y GANANCIAS', 3, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '61';

-- =============================================================================
-- NIVEL 4 - SUBCUENTAS (6 digitos)
-- =============================================================================
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110101', 'CAJA', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110102', 'BANCOS MONEDA NACIONAL', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110201', 'CUENTAS POR COBRAR CRÉDITOS OTORGADOS', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110202', 'OTRAS CUENTAS POR COBRAR', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110204', 'DOCUMENTOS POR COBRAR', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110501', 'Bodega sucursal 01', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110502', 'Bodega sucursal 02', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110504', 'Mercadería en Tránsito', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110701', 'Seguros pagados por anticipado', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1107';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110702', 'Alquileres pagados por anticipado', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1107';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110703', 'Papelería y útiles', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1107';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110901', 'Compras Locales', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1109';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '110902', 'Importaciones', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1109';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '120101', 'Terrenos', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '120102', 'Edificios', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '120103', 'MOBILIARIO Y EQUIPO', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '1201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '120106', 'Equipo de transporte', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '1201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '120108', 'DEPRECIACIÓN ACUMULADA - PROPIEDADES, PLANTA Y EQUIPO (CR)', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '1201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210101', 'Sobregiros bancarios', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210102', 'Préstamos bancarios (porción a corto plazo)', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210201', 'PROVEEDORES POR PAGAR', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '2102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210202', 'DOCUMENTOS POR PAGAR', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210301', 'Cuota patronal ISSS', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210302', 'Cuota patronal AFP', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210303', 'IVA por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210305', 'Impuestos municipales', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210307', 'Intereses por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210309', 'Alquileres por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210311', 'Anticipos de clientes', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210401', 'Cotización ISSS / Salud', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2104';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210402', 'COTIZACIÓN A FONDOS DE PENSIONES', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '2104';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210403', 'RETENCIÓN DE IMPUESTO SOBRE LA RENTA', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '2104';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210501', 'Sueldos por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210504', 'Vacaciones por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210505', 'Aguinaldos por pagar', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2105';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210601', 'Impuesto sobre la Renta Anual', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2106';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '210801', 'IVA - Débito Fiscal', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2108';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '220101', 'Préstamos bancarios (porción a largo plazo)', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '2201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '310101', 'CAPITAL SOCIAL MÍNIMO', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '3101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '320101', 'Utilidades de Ejercicios Anteriores', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '3201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '320201', 'Utilidad del presente ejercicio', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '3202';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '320301', 'Déficit de ejercicios anteriores', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '3203';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '320401', 'Déficit del presente ejercicio', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '3204';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410101', 'Costo de venta mercadería adquirida para la venta', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '4101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410201', 'GASTOS DE PERSONAL', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410203', 'GASTOS POR SERVICIOS PÚBLICOS Y PRIVADOS', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410204', 'HONORARIOS PROFESIONALES', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410205', 'GASTOS POR DEPRECIACIÓN', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410207', 'GASTOS POR SEGUROS', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410208', 'GASTOS POR IMPUESTOS, TASAS MUNICIPALES Y OTRAS CONTRIBUCIONES', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410210', 'GASTOS DE VIÁTICOS, VIAJES Y DE REPRESENTACIÓN', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410301', 'GASTOS DE PERSONAL (VENTAS)', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '410303', 'GASTOS POR SERVICIOS PÚBLICOS Y PRIVADOS (VENTAS)', 4, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '4103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '420101', 'Intereses sobre préstamos', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '4201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '420102', 'Comisiones', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '4201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '420201', 'Pérdida en venta o retiro de activos fijos', 4, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '4202';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '510101', 'VENTAS LOCALES', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '5101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '510104', 'REBAJAS Y DEVOLUCIONES SOBRE VENTAS', 4, id, 'ACREEDOR', FALSE FROM cuentas WHERE codigo = '5101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '520101', 'Intereses bancarios', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '5201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '520201', 'Ganancia en venta de activos fijos', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '5202';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '610101', 'Pérdidas y ganancias', 4, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '6101';

-- =============================================================================
-- NIVEL 5 - CUENTAS DE DETALLE (8 digitos) - aceptan movimientos
-- =============================================================================
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11010101', 'Caja General', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11010102', 'Caja Chica', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11010201', 'CUENTA CORRIENTE', 5, id, 'DEUDOR', FALSE FROM cuentas WHERE codigo = '110102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11010202', 'CUENTA DE AHORRO', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11010203', 'DEPÓSITOS A PLAZO', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110102';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11020101', 'CUENTAS POR COBRAR CLIENTES', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '11020201', 'VENTA CON TARJETA DE CRÉDITO', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '110202';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010301', 'Mobiliario y equipo de Oficina', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '120103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010302', 'Equipo de cómputo', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '120103';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010801', 'Depreciación acumulada de Edificios (CR)', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '120108';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010802', 'Depreciación acumulada de Mobiliario y equipo (CR)', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '120108';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010803', 'Depreciación acumulada de Instalaciones y mejoras (CR)', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '120108';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '12010804', 'Depreciación acumulada de Equipo de transporte (CR)', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '120108';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21020101', 'PROVEEDORES NACIONALES', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '210201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21040201', 'ISSS provisional', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '210402';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21040202', 'AFP Crecer', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '210402';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21040203', 'AFP Confía', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '210402';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '21040301', 'Retención con subordinación laboral', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '210403';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020101', 'Salarios', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020102', 'Vacaciones', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020103', 'Aguinaldos', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020108', 'Cuota patronal seguridad social ISSS', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020109', 'Cuota patronal fondo de pensiones AFP', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020110', 'INSAFORP', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020301', 'Servicio de agua', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410203';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020302', 'Servicio de energía eléctrica', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410203';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020303', 'Servicio de teléfono', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410203';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020304', 'Servicio de internet/cable', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410203';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020401', 'Honorarios legales', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410204';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020402', 'Honorarios contables', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410204';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020501', 'Depreciación de edificaciones', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410205';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020505', 'Depreciación de Mobiliario y Equipo de Oficina', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410205';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020507', 'Depreciación de Equipo de transporte', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410205';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020702', 'Seguro de activos', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410207';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41020801', 'Impuestos y tasas municipales', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410208';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41021007', 'Combustibles y lubricantes', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410210';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41021008', 'FOVIAL', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410210';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41021009', 'Alquileres', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410210';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41021010', 'Papelería y útiles', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410210';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41030101', 'Salarios', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410301';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '41030306', 'Publicidad y promoción', 5, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '410303';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '51010101', 'Ventas a contribuyentes', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '510101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '51010102', 'Ventas a consumidor final', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '510101';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '51010401', 'Rebajas sobre ventas', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '510104';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '51010402', 'Devoluciones sobre ventas', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '510104';

-- =============================================================================
-- NIVEL 6 - SUBCUENTA ANALITICA (10 digitos) - ejemplo minimo de la estructura
-- =============================================================================
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1101020101', 'Banco de América Central, S.A.', 6, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '11010201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1101020103', 'Banco Davivienda, S.A.', 6, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '11010201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1101020105', 'Banco Agrícola, S.A.', 6, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '11010201';
INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '1101020106', 'Banco Promerica', 6, id, 'DEUDOR', TRUE FROM cuentas WHERE codigo = '11010201';
