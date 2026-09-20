package com.finanzassv.service;

import com.finanzassv.dto.cuenta.CuentaRequest;
import com.finanzassv.dto.cuenta.CuentaResponse;
import com.finanzassv.entity.Cuenta;
import com.finanzassv.exception.RecursoNoEncontradoException;
import com.finanzassv.exception.ReglaNegocioException;
import com.finanzassv.repository.AsientoDetalleRepository;
import com.finanzassv.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion del Catalogo de Cuentas (arbol jerarquico).
 *
 * Regla de negocio: las cuentas principales (con subcuentas) NO aceptan
 * movimientos; solo las cuentas hoja (secundarias) reciben montos.
 */
@Service
public class CatalogoService {

    private final CuentaRepository cuentaRepository;
    private final AsientoDetalleRepository detalleRepository;

    public CatalogoService(CuentaRepository cuentaRepository, AsientoDetalleRepository detalleRepository) {
        this.cuentaRepository = cuentaRepository;
        this.detalleRepository = detalleRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> catalogo() {
        return cuentaRepository.findAllByOrderByCodigoAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> cuentasDeMovimiento() {
        return cuentaRepository.findByAceptaMovimientosTrueAndActivoTrueOrderByCodigoAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsByCodigo(request.codigo())) {
            throw new ReglaNegocioException("Ya existe una cuenta con el codigo: " + request.codigo());
        }

        Cuenta padre = null;
        int nivel;
        if (request.cuentaPadreId() == null) {
            nivel = 1;
        } else {
            padre = cuentaRepository.findById(request.cuentaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Cuenta padre no encontrada: " + request.cuentaPadreId()));
            if (Boolean.TRUE.equals(padre.getAceptaMovimientos())) {
                throw new ReglaNegocioException(
                        "No se puede crear una subcuenta bajo una cuenta de movimiento");
            }
            nivel = padre.getNivel() + 1;
        }

        if (request.naturaleza() == null) {
            throw new ReglaNegocioException("La naturaleza de la cuenta es obligatoria");
        }

        var cuenta = Cuenta.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .nivel(nivel)
                .cuentaPadre(padre)
                .naturaleza(request.naturaleza())
                .aceptaMovimientos(request.aceptaMovimientos() != null && request.aceptaMovimientos())
                .activo(request.activo() == null || request.activo())
                .build();
        return aRespuesta(cuentaRepository.save(cuenta));
    }

    @Transactional
    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        var cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + id));

        cuenta.setNombre(request.nombre());
        if (request.activo() != null) {
            cuenta.setActivo(request.activo());
        }
        if (request.aceptaMovimientos() != null
                && !request.aceptaMovimientos().equals(cuenta.getAceptaMovimientos())) {
            if (Boolean.TRUE.equals(request.aceptaMovimientos()) && !cuenta.esHoja()) {
                throw new ReglaNegocioException(
                        "Solo las cuentas sin subcuentas pueden aceptar movimientos");
            }
            cuenta.setAceptaMovimientos(request.aceptaMovimientos());
        }
        return aRespuesta(cuentaRepository.save(cuenta));
    }

    @Transactional
    public void desactivar(Long id) {
        var cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + id));
        if (cuentaRepository.existsByCuentaPadreId(id)) {
            throw new ReglaNegocioException(
                    "La cuenta tiene subcuentas. Desactive primero las subcuentas.");
        }
        if (!detalleRepository.findByCuentaId(id).isEmpty()) {
            throw new ReglaNegocioException(
                    "La cuenta tiene movimientos registrados en asientos");
        }
        cuenta.setActivo(false);
        cuentaRepository.save(cuenta);
    }

    private CuentaResponse aRespuesta(Cuenta c) {
        return new CuentaResponse(
                c.getId(),
                c.getCodigo(),
                c.getNombre(),
                c.getNivel(),
                c.getCuentaPadre() != null ? c.getCuentaPadre().getId() : null,
                c.getCuentaPadre() != null ? c.getCuentaPadre().getNombre() : null,
                c.getNaturaleza(),
                c.getAceptaMovimientos(),
                c.getSaldoDebe(),
                c.getSaldoHaber(),
                c.saldoNeto(),
                c.getSaldoDebe().compareTo(c.getSaldoHaber()) > 0 ? "DEUDOR" : "ACREEDOR",
                c.getActivo());
    }
}
