package com.finanzassv.service;

import com.finanzassv.dto.asiento.AsientoRequest;
import com.finanzassv.dto.asiento.AsientoResumenResponse;
import com.finanzassv.dto.asiento.AsientoResponse;
import com.finanzassv.dto.asiento.LineaAsientoRequest;
import com.finanzassv.dto.asiento.LineaAsientoResponse;
import com.finanzassv.dto.asiento.LineaMayorResponse;
import com.finanzassv.entity.Asiento;
import com.finanzassv.entity.AsientoDetalle;
import com.finanzassv.entity.Cuenta;
import com.finanzassv.entity.Usuario;
import com.finanzassv.enums.EstadoAsiento;
import com.finanzassv.exception.RecursoNoEncontradoException;
import com.finanzassv.exception.ReglaNegocioException;
import com.finanzassv.repository.AsientoDetalleRepository;
import com.finanzassv.repository.AsientoRepository;
import com.finanzassv.repository.CuentaRepository;
import com.finanzassv.repository.UsuarioRepository;
import com.finanzassv.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro de asientos del Libro Diario.
 *
 * Reglas de negocio criticas:
 * 1. Ley de la Partida Doble: la suma del Debe debe ser EXACTAMENTE igual
 *    a la suma del Haber. El asiento se rechaza con error si no se cumple.
 * 2. Solo se admiten cuentas de movimiento (secundarias, hoja del catalogo).
 * 3. Mayorizacion automatica: al guardar, los montos se acumulan en la
 *    cuenta secundaria Y en todas sus cuentas principales (ancestros),
 *    de modo que las principales llevan el control del Debe y Haber.
 */
@Service
public class AsientoService {

    private final AsientoRepository asientoRepository;
    private final AsientoDetalleRepository detalleRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsientoService(AsientoRepository asientoRepository, AsientoDetalleRepository detalleRepository,
                          CuentaRepository cuentaRepository, UsuarioRepository usuarioRepository) {
        this.asientoRepository = asientoRepository;
        this.detalleRepository = detalleRepository;
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AsientoResponse registrar(AsientoRequest request, UsuarioPrincipal autenticado) {
        // 1. Resolver cuentas y validar que sean de movimiento y esten activas
        Map<Long, Cuenta> cuentas = resolverCuentas(request.lineas());

        // 2. Validar cada linea: monto en EXACTAMENTE una columna
        for (int i = 0; i < request.lineas().size(); i++) {
            validarLinea(request.lineas().get(i), i + 1);
        }

        // 3. REGLA CRITICA: Ley de la Partida Doble (Debe = Haber)
        BigDecimal totalDebe = sumarColumna(request.lineas(), true);
        BigDecimal totalHaber = sumarColumna(request.lineas(), false);
        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new ReglaNegocioException(String.format(
                    "La Ley de la Partida Doble no se cumple: Debe = %s y Haber = %s. "
                            + "Las sumas deben ser exactamente iguales.",
                    totalDebe, totalHaber));
        }

        // 4. Usuario que registra
        Usuario usuario = usuarioRepository.findById(autenticado.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // 5. Numeracion automatica del asiento
        Long numero = asientoRepository.findFirstByOrderByNumeroAsientoDesc()
                .map(a -> a.getNumeroAsiento() + 1)
                .orElse(1L);

        // 6. Construir asiento y lineas
        var asiento = Asiento.builder()
                .numeroAsiento(numero)
                .fecha(request.fecha())
                .concepto(request.concepto())
                .usuario(usuario)
                .totalDebe(totalDebe)
                .totalHaber(totalHaber)
                .build();

        List<AsientoDetalle> detalles = new ArrayList<>();
        int orden = 1;
        for (LineaAsientoRequest linea : request.lineas()) {
            var detalle = AsientoDetalle.builder()
                    .asiento(asiento)
                    .cuenta(cuentas.get(linea.cuentaId()))
                    .concepto(linea.concepto() == null || linea.concepto().isBlank()
                            ? request.concepto() : linea.concepto())
                    .montoDebe(linea.montoDebe() == null ? BigDecimal.ZERO : linea.montoDebe())
                    .montoHaber(linea.montoHaber() == null ? BigDecimal.ZERO : linea.montoHaber())
                    .ordenLinea(orden++)
                    .build();
            detalles.add(detalle);
        }
        asiento.setDetalles(detalles);

        // 7. Guardar el asiento (cascade guarda las lineas)
        asiento = asientoRepository.save(asiento);

        // 8. MAYORIZACION AUTOMATICA: acumular en la cuenta y en todos sus
        //    ancestros (cuentas principales llevan el control del Debe/Haber)
        mayorizar(detalles);

        return aRespuesta(asiento);
    }

    /** Anula un asiento y revierte la mayorizacion de los saldos. */
    @Transactional
    public AsientoResponse anular(Long id, UsuarioPrincipal autenticado) {
        var asiento = asientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asiento no encontrado: " + id));
        if (asiento.getEstado() == EstadoAsiento.ANULADO) {
            throw new ReglaNegocioException("El asiento ya esta anulado");
        }
        asiento.setEstado(EstadoAsiento.ANULADO);
        asientoRepository.save(asiento);

        // Revertir saldos en las cuentas afectadas y sus ancestros
        for (AsientoDetalle detalle : asiento.getDetalles()) {
            Cuenta actual = detalle.getCuenta();
            while (actual != null) {
                actual.setSaldoDebe(actual.getSaldoDebe().subtract(detalle.getMontoDebe()));
                actual.setSaldoHaber(actual.getSaldoHaber().subtract(detalle.getMontoHaber()));
                actual = actual.getCuentaPadre();
            }
        }
        cuentaRepository.saveAll(cuentasAfectadas(asiento));
        return aRespuesta(asiento);
    }

    @Transactional(readOnly = true)
    public List<AsientoResumenResponse> listado() {
        return asientoRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getNumeroAsiento(), b.getNumeroAsiento()))
                .map(a -> new AsientoResumenResponse(
                        a.getId(), a.getNumeroAsiento(), a.getFecha(), a.getConcepto(),
                        a.getUsuario().getNombreCompleto(),
                        a.getTotalDebe(), a.getTotalHaber(), a.getEstado()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AsientoResponse detalle(Long id) {
        var asiento = asientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asiento no encontrado: " + id));
        return aRespuesta(asiento);
    }

    /**
     * Libro Mayor de una cuenta.
     *
     * REGLA DE NEGOCIO: las cuentas PRINCIPALES (de control) registran el
     * control del Debe y Haber: su Libro Mayor consolida, por asiento, los
     * movimientos de TODAS sus cuentas secundarias (una linea por asiento con
     * la suma). Las cuentas de movimiento muestran cada partida en parcial.
     */
    @Transactional(readOnly = true)
    public List<LineaMayorResponse> mayorPorCuenta(Long cuentaId) {
        var raiz = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + cuentaId));

        // Sub-arbol: la cuenta y TODAS sus descendientes
        List<Long> idsSubarbol = new ArrayList<>();
        java.util.Deque<Cuenta> pila = new java.util.ArrayDeque<>();
        pila.push(raiz);
        while (!pila.isEmpty()) {
            Cuenta actual = pila.pop();
            idsSubarbol.add(actual.getId());
            if (actual.getSubCuentas() != null) {
                actual.getSubCuentas().forEach(pila::push);
            }
        }

        Map<Asiento, List<AsientoDetalle>> porAsiento = detalleRepository
                .findByCuentaIdIn(idsSubarbol).stream()
                .filter(d -> d.getAsiento().getEstado() == EstadoAsiento.REGISTRADO)
                .collect(Collectors.groupingBy(AsientoDetalle::getAsiento,
                        java.util.LinkedHashMap::new, Collectors.toList()));

        return porAsiento.values().stream()
                .sorted(java.util.Comparator
                        .comparing((List<AsientoDetalle> l) -> l.get(0).getAsiento().getFecha())
                        .thenComparing(l -> l.get(0).getAsiento().getNumeroAsiento()))
                .map(lineas -> {
                    var asiento = lineas.get(0).getAsiento();
                    BigDecimal debe = lineas.stream()
                            .map(AsientoDetalle::getMontoDebe)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal haber = lineas.stream()
                            .map(AsientoDetalle::getMontoHaber)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String conceptoLinea = lineas.size() == 1
                            ? lineas.get(0).getConcepto()
                            : "Consolidado de " + lineas.size() + " cuentas de movimiento";
                    return new LineaMayorResponse(
                            asiento.getNumeroAsiento(), asiento.getFecha(),
                            asiento.getConcepto(), conceptoLinea, debe, haber);
                })
                .toList();
    }

    // ------------------------------------------------------------------
    // Validaciones y helpers
    // ------------------------------------------------------------------

    /** Carga las cuentas de las lineas y valida movimiento + estado activo. */
    private Map<Long, Cuenta> resolverCuentas(List<LineaAsientoRequest> lineas) {
        Set<Long> ids = new HashSet<>();
        lineas.forEach(l -> {
            if (l.cuentaId() == null) {
                throw new ReglaNegocioException("Cada linea debe indicar una cuenta");
            }
            ids.add(l.cuentaId());
        });
        Map<Long, Cuenta> cuentas = cuentaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cuenta::getId, Function.identity()));
        for (Long id : ids) {
            var cuenta = cuentas.get(id);
            if (cuenta == null) {
                throw new ReglaNegocioException("La cuenta indicada no existe (id " + id + ")");
            }
            if (Boolean.FALSE.equals(cuenta.getActivo())) {
                throw new ReglaNegocioException(
                        "La cuenta " + cuenta.getCodigo() + " " + cuenta.getNombre() + " esta desactivada");
            }
            if (Boolean.FALSE.equals(cuenta.getAceptaMovimientos())) {
                throw new ReglaNegocioException(String.format(
                        "La cuenta %s %s es una cuenta PRINCIPAL (de control) y no acepta movimientos. "
                                + "Registre el asiento en una cuenta secundaria.",
                        cuenta.getCodigo(), cuenta.getNombre()));
            }
        }
        return cuentas;
    }

    /** Cada linea mueve monto en EXACTAMENTE una de las dos columnas. */
    private void validarLinea(LineaAsientoRequest linea, int numero) {
        BigDecimal debe = linea.montoDebe() == null ? BigDecimal.ZERO : linea.montoDebe();
        BigDecimal haber = linea.montoHaber() == null ? BigDecimal.ZERO : linea.montoHaber();
        boolean conDebe = debe.compareTo(BigDecimal.ZERO) > 0;
        boolean conHaber = haber.compareTo(BigDecimal.ZERO) > 0;
        if (conDebe && conHaber) {
            throw new ReglaNegocioException(
                    "La linea " + numero + " tiene monto en Debe y Haber a la vez");
        }
        if (!conDebe && !conHaber) {
            throw new ReglaNegocioException(
                    "La linea " + numero + " debe tener un monto en Debe o en Haber");
        }
    }

    private BigDecimal sumarColumna(List<LineaAsientoRequest> lineas, boolean columnaDebe) {
        return lineas.stream()
                .map(l -> Boolean.TRUE.equals(columnaDebe) ? l.montoDebe() : l.montoHaber())
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Mayorizacion: para cada linea, acumula el monto en la cuenta secundaria
     * y propaga el total hacia arriba por todas sus cuentas principales.
     */
    private void mayorizar(List<AsientoDetalle> detalles) {
        Set<Long> afectadas = new HashSet<>();
        for (AsientoDetalle detalle : detalles) {
            Cuenta actual = detalle.getCuenta();
            while (actual != null) {
                actual.setSaldoDebe(actual.getSaldoDebe().add(detalle.getMontoDebe()));
                actual.setSaldoHaber(actual.getSaldoHaber().add(detalle.getMontoHaber()));
                afectadas.add(actual.getId());
                actual = actual.getCuentaPadre();
            }
        }
        // Persistir el estado actualizado de las cuentas afectadas
        cuentaRepository.saveAll(afectadas.stream().map(cuentaRepository::findById)
                .filter(Optional::isPresent).map(Optional::get).toList());
    }

    /** Recoleccion de cuentas unicas tocadas por un asiento (para revertir). */
    private Set<Cuenta> cuentasAfectadas(Asiento asiento) {
        Set<Cuenta> cuentas = new HashSet<>();
        for (AsientoDetalle detalle : asiento.getDetalles()) {
            Cuenta actual = detalle.getCuenta();
            while (actual != null) {
                cuentas.add(actual);
                actual = actual.getCuentaPadre();
            }
        }
        return cuentas;
    }

    private AsientoResponse aRespuesta(Asiento asiento) {
        List<LineaAsientoResponse> lineas = asiento.getDetalles().stream()
                .map(d -> new LineaAsientoResponse(
                        d.getId(), d.getCuenta().getId(),
                        d.getCuenta().getCodigo(), d.getCuenta().getNombre(),
                        d.getConcepto(), d.getMontoDebe(), d.getMontoHaber(), d.getOrdenLinea()))
                .toList();
        return new AsientoResponse(
                asiento.getId(), asiento.getNumeroAsiento(), asiento.getFecha(),
                asiento.getConcepto(), asiento.getUsuario().getId(),
                asiento.getUsuario().getNombreCompleto(),
                asiento.getTotalDebe(), asiento.getTotalHaber(),
                asiento.getEstado(), asiento.getFechaCreacion(), lineas);
    }
}
