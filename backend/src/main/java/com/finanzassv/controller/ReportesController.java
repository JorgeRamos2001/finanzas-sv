package com.finanzassv.controller;

import com.finanzassv.dto.reporte.BalanceGeneralResponse;
import com.finanzassv.dto.reporte.EstadoResultadosResponse;
import com.finanzassv.service.ReportesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Estados financieros generados dinamicamente:
 * Balance General (1, 2, 3) y Estado de Resultados (4, 5).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    private final ReportesService reportesService;

    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    @GetMapping("/balance-general")
    public BalanceGeneralResponse balanceGeneral() {
        return reportesService.balanceGeneral();
    }

    @GetMapping("/estado-resultados")
    public EstadoResultadosResponse estadoResultados() {
        return reportesService.estadoResultados();
    }
}
