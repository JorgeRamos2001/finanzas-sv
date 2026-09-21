-- =============================================================================
--  FinanzasSV - V3: Codigos de cuenta sin punto separador
--  Rama: feat/codigos-sin-punto
-- =============================================================================
--  Formato de codigo elegido por el usuario: sin punto separador.
--    1      Activo
--    11     Efectivo y Equivalentes
--    111    Caja
--    112    Bancos
--    421    Sueldos y Salarios
--  La clasificacion por primer digito (1 Activo, 2 Pasivo, 3 Capital,
--  4 Costos y Gastos, 5 Ingresos) no cambia.
-- =============================================================================

UPDATE cuentas
SET codigo = REPLACE(codigo, '.', '')
WHERE codigo LIKE '%.%';
