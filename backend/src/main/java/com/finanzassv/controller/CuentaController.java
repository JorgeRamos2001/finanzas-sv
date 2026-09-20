package com.finanzassv.controller;

import com.finanzassv.dto.cuenta.CuentaRequest;
import com.finanzassv.dto.cuenta.CuentaResponse;
import com.finanzassv.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catalogo de cuentas (arbol contable).
 * Lectura: todos los roles. Escritura: solo ADMIN.
 */
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CatalogoService catalogoService;

    public CuentaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /** Catalogo completo con saldos consolidados (Libro Mayor dinamico). */
    @GetMapping
    public List<CuentaResponse> catalogo() {
        return catalogoService.catalogo();
    }

    /** Solo cuentas de movimiento activas (para el formulario de asientos). */
    @GetMapping("/movimiento")
    public List<CuentaResponse> cuentasDeMovimiento() {
        return catalogoService.cuentasDeMovimiento();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crear(@Valid @RequestBody CuentaRequest request) {
        return catalogoService.crear(request);
    }

    @PutMapping("/{id}")
    public CuentaResponse actualizar(@PathVariable Long id,
                                     @Valid @RequestBody CuentaRequest request) {
        return catalogoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Long id) {
        catalogoService.desactivar(id);
    }
}
