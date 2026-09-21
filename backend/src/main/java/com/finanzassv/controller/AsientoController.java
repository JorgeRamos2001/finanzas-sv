package com.finanzassv.controller;

import com.finanzassv.dto.asiento.AsientoRequest;
import com.finanzassv.dto.asiento.AsientoResumenResponse;
import com.finanzassv.dto.asiento.AsientoResponse;
import com.finanzassv.dto.asiento.LineaMayorResponse;
import com.finanzassv.security.UsuarioPrincipal;
import com.finanzassv.service.AsientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Libro Diario: registro, listado, detalle y anulacion de asientos.
 */
@RestController
@RequestMapping("/api/asientos")
public class AsientoController {

    private final AsientoService asientoService;

    public AsientoController(AsientoService asientoService) {
        this.asientoService = asientoService;
    }

    @GetMapping
    public List<AsientoResumenResponse> listado() {
        return asientoService.listado();
    }

    @GetMapping("/{id}")
    public AsientoResponse detalle(@PathVariable Long id) {
        return asientoService.detalle(id);
    }

    /** Libro Mayor: movimientos Debe/Haber de una cuenta especifica. */
    @GetMapping("/cuenta/{cuentaId}")
    public List<LineaMayorResponse> mayorPorCuenta(@PathVariable Long cuentaId) {
        return asientoService.mayorPorCuenta(cuentaId);
    }

    /** Rechaza con HTTP 400 si no se cumple la Ley de la Partida Doble. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsientoResponse registrar(@Valid @RequestBody AsientoRequest request,
                                     @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return asientoService.registrar(request, usuario);
    }

    @PostMapping("/{id}/anular")
    public AsientoResponse anular(@PathVariable Long id,
                                  @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return asientoService.anular(id, usuario);
    }
}
