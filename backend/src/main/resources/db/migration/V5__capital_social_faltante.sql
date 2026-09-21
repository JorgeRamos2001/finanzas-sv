-- =============================================================================
--  FinanzasSV - V5: Cuentas faltantes del capital social (catalogo de clase)
--  Rama: feat/catalogo-comercial-clase
-- =============================================================================

INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '31010101', 'Capital Social Mínimo pagado', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '310101';

INSERT INTO cuentas (codigo, nombre, nivel, cuenta_padre_id, naturaleza, acepta_movimientos)
SELECT '31010102', 'Capital Social Mínimo por pagar', 5, id, 'ACREEDOR', TRUE FROM cuentas WHERE codigo = '310101';
