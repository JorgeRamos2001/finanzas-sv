package com.finanzassv.service;

import com.finanzassv.dto.reporte.BalanceGeneralResponse;
import com.finanzassv.dto.reporte.EstadoResultadosResponse;
import com.finanzassv.dto.reporte.LineaReporte;
import com.finanzassv.entity.Cuenta;
import com.finanzassv.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Generacion dinamica de estados financieros.
 *
 * Clasificacion por el PRIMER DIGITO del codigo de cuenta:
 *   1 = Activo, 2 = Pasivo, 3 = Capital Contable  -> Balance General
 *   4 = Costos y Gastos, 5 = Ingresos             -> Estado de Resultados
 *
 * Se usan las cuentas principales (nivel 2, de control) que ya consolidan
 * automaticamente los movimientos de sus cuentas secundarias.
 */
@Service
public class ReportesService {

    private final CuentaRepository cuentaRepository;

    public ReportesService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional(readOnly = true)
    public BalanceGeneralResponse balanceGeneral() {
        List<Cuenta> todas = cuentaRepository.findAllByOrderByCodigoAsc();

        List<LineaReporte> activos = grupo(todas, '1');
        List<LineaReporte> pasivos = grupo(todas, '2');
        List<LineaReporte> capital = grupo(todas, '3');

        // Activo: naturaleza DEUDOR -> total = Debe - Haber
        BigDecimal totalActivo = activos.stream()
                .map(LineaReporte::saldoNeto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Pasivo y Capital: naturaleza ACREEDOR -> total = Haber - Debe
        BigDecimal totalPasivo = saldosAcreedores(pasivos);
        BigDecimal totalCapital = saldosAcreedores(capital);

        // Utilidad del periodo calculada del Estado de Resultados
        BigDecimal utilidad = utilidadPeriodo(todas);

        // Ecuacion contable: Activo = Pasivo + Capital Contable + Utilidad
        BigDecimal pasivoMasCapital = totalPasivo.add(totalCapital);
        BigDecimal esperado = pasivoMasCapital.add(utilidad);
        BigDecimal diferencia = totalActivo.subtract(esperado);
        boolean cumple = diferencia.compareTo(BigDecimal.ZERO) == 0;

        return new BalanceGeneralResponse(
                activos, pasivos, capital,
                totalActivo, totalPasivo, totalCapital,
                utilidad, esperado, cumple, diferencia);
    }

    @Transactional(readOnly = true)
    public EstadoResultadosResponse estadoResultados() {
        List<Cuenta> todas = cuentaRepository.findAllByOrderByCodigoAsc();
        List<LineaReporte> ingresos = grupo(todas, '5');
        List<LineaReporte> gastos = grupo(todas, '4');

        // Ingresos: naturaleza ACREEDOR -> Haber - Debe
        BigDecimal totalIngresos = ingresos.stream()
                .map(l -> l.saldoHaber().subtract(l.saldoDebe()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Costos y Gastos: naturaleza DEUDOR -> Debe - Haber
        BigDecimal totalGastos = gastos.stream()
                .map(l -> l.saldoDebe().subtract(l.saldoHaber()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal utilidad = totalIngresos.subtract(totalGastos);
        return new EstadoResultadosResponse(
                ingresos, gastos, totalIngresos, totalGastos, utilidad,
                utilidad.compareTo(BigDecimal.ZERO) >= 0 ? "UTILIDAD" : "PERDIDA");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Agrupa las cuentas PRINCIPALES (nivel 2) por primer digito del codigo. */
    private List<LineaReporte> grupo(List<Cuenta> cuentas, char digito) {
        return cuentas.stream()
                .filter(c -> c.getNivel() != null && c.getNivel() == 2)
                .filter(c -> !c.getCodigo().isEmpty() && c.getCodigo().charAt(0) == digito)
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(this::aLinea)
                .toList();
    }

    /** Suma Haber - Debe de cada linea (naturaleza acreedora). */
    private BigDecimal saldosAcreedores(List<LineaReporte> lineas) {
        return lineas.stream()
                .map(l -> l.saldoHaber().subtract(l.saldoDebe()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LineaReporte aLinea(Cuenta c) {
        return new LineaReporte(
                c.getCodigo(), c.getNombre(), c.getNaturaleza(),
                c.getSaldoDebe(), c.getSaldoHaber(), c.saldoNeto(),
                c.getSaldoDebe().compareTo(c.getSaldoHaber()) > 0 ? "DEUDOR" : "ACREEDOR");
    }

    /** Utilidad del periodo: Ingresos (5) - Costos y Gastos (4). */
    private BigDecimal utilidadPeriodo(List<Cuenta> cuentas) {
        BigDecimal ingresos = cuentas.stream()
                .filter(c -> c.getNivel() != null && c.getNivel() == 2
                        && !c.getCodigo().isEmpty() && c.getCodigo().charAt(0) == '5')
                .map(c -> c.getSaldoHaber().subtract(c.getSaldoDebe()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastos = cuentas.stream()
                .filter(c -> c.getNivel() != null && c.getNivel() == 2
                        && !c.getCodigo().isEmpty() && c.getCodigo().charAt(0) == '4')
                .map(c -> c.getSaldoDebe().subtract(c.getSaldoHaber()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ingresos.subtract(gastos);
    }
}
