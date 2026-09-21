package com.finanzassv.service;

import com.finanzassv.dto.asiento.AsientoRequest;
import com.finanzassv.dto.asiento.LineaAsientoRequest;
import com.finanzassv.entity.Asiento;
import com.finanzassv.entity.Cuenta;
import com.finanzassv.entity.Usuario;
import com.finanzassv.enums.NaturalezaCuenta;
import com.finanzassv.exception.ReglaNegocioException;
import com.finanzassv.repository.AsientoRepository;
import com.finanzassv.repository.CuentaRepository;
import com.finanzassv.repository.UsuarioRepository;
import com.finanzassv.security.UsuarioPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la Ley de la Partida Doble y de la mayorizacion automatica.
 */
class AsientoServiceTest {

    private AsientoRepository asientoRepository;
    private com.finanzassv.repository.AsientoDetalleRepository detalleRepository;
    private CuentaRepository cuentaRepository;
    private UsuarioRepository usuarioRepository;
    private AsientoService service;

    // Jerarquia: 1 Activo -> 1.1 Efectivo y Equivalentes -> 1.1.1 Caja
    private Cuenta activo;
    private Cuenta efectivo;
    private Cuenta caja;
    private Cuenta ingresos;
    private Cuenta ventas;
    private Cuenta ventasLocales;

    @BeforeEach
    void setUp() {
        asientoRepository = mock(AsientoRepository.class);
        detalleRepository = mock(com.finanzassv.repository.AsientoDetalleRepository.class);
        cuentaRepository = mock(CuentaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AsientoService(asientoRepository, detalleRepository, cuentaRepository,
                usuarioRepository);

        activo = cuenta(1L, "1", "Activo", 1, null);
        efectivo = cuenta(6L, "1.1", "Efectivo y Equivalentes", 2, activo);
        efectivo.setAceptaMovimientos(false);
        caja = cuenta(18L, "1.1.1", "Caja", 3, efectivo);
        ingresos = cuenta(5L, "5", "Ingresos", 1, null, NaturalezaCuenta.ACREEDOR);
        ventas = cuenta(16L, "5.1", "Ventas", 2, ingresos, NaturalezaCuenta.ACREEDOR);
        ventasLocales = cuenta(33L, "5.1.1", "Ventas Locales", 3, ventas, NaturalezaCuenta.ACREEDOR);

        when(cuentaRepository.findAllById(anyIterable()))
                .thenReturn(List.of(caja, ventasLocales));
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(Usuario.builder().id(1L).username("contador")
                        .nombreCompleto("Contador").build()));
        when(asientoRepository.findFirstByOrderByNumeroAsientoDesc()).thenReturn(Optional.empty());
        when(asientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cuentaRepository.findById(any())).thenAnswer(inv ->
                Optional.of(cuentaPorId((Long) inv.getArgument(0))));
        when(cuentaRepository.saveAll(anyIterable())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Cuenta cuentaPorId(Long id) {
        if (id.equals(activo.getId())) return activo;
        if (id.equals(efectivo.getId())) return efectivo;
        if (id.equals(caja.getId())) return caja;
        if (id.equals(ingresos.getId())) return ingresos;
        if (id.equals(ventas.getId())) return ventas;
        return ventasLocales;
    }

    private Cuenta cuenta(Long id, String codigo, String nombre, int nivel, Cuenta padre) {
        return cuenta(id, codigo, nombre, nivel, padre, NaturalezaCuenta.DEUDOR);
    }

    private Cuenta cuenta(Long id, String codigo, String nombre, int nivel, Cuenta padre,
                          NaturalezaCuenta naturaleza) {
        return Cuenta.builder()
                .id(id).codigo(codigo).nombre(nombre).nivel(nivel)
                .cuentaPadre(padre).naturaleza(naturaleza)
                .aceptaMovimientos(true)
                .saldoDebe(BigDecimal.ZERO).saldoHaber(BigDecimal.ZERO)
                .activo(true)
                .build();
    }

    private UsuarioPrincipal autenticado() {
        return new UsuarioPrincipal(1L, "contador", "CONTADOR", "Contador");
    }

    private AsientoRequest asiento(BigDecimal debe, BigDecimal haber) {
        return new AsientoRequest(LocalDate.now(), "Venta al contado",
                List.of(
                        new LineaAsientoRequest(18L, "Ingreso a Caja", debe, BigDecimal.ZERO),
                        new LineaAsientoRequest(33L, "Venta", BigDecimal.ZERO, haber)));
    }

    @Test
    @DisplayName("RECHAZA asiento desbalanceado (Ley de la Partida Doble)")
    void rechazaAsientoDesbalanceado() {
        assertThatThrownBy(() -> service.registrar(asiento(
                new BigDecimal("500.00"), new BigDecimal("450.00")), autenticado()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Partida Doble");
    }

    @Test
    @DisplayName("ACEPTA asiento balanceado y mayoriza la cuenta secundaria")
    void aceptaAsientoBalanceadoYMayoriza() {
        var respuesta = service.registrar(
                asiento(new BigDecimal("500.00"), new BigDecimal("500.00")), autenticado());

        assertThat(respuesta.numeroAsiento()).isEqualTo(1L);
        assertThat(respuesta.totalDebe()).isEqualByComparingTo("500.00");
        assertThat(respuesta.totalHaber()).isEqualByComparingTo("500.00");
        assertThat(caja.getSaldoDebe()).isEqualByComparingTo("500.00");
        assertThat(ventasLocales.getSaldoHaber()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Mayorizacion propaga el total a las cuentas principales (1.1 y 1)")
    void mayorizacionPropagaAAncestros() {
        service.registrar(asiento(new BigDecimal("500.00"), new BigDecimal("500.00")), autenticado());

        ArgumentCaptor<Iterable<Cuenta>> captor = ArgumentCaptor.forClass(Iterable.class);
        org.mockito.Mockito.verify(cuentaRepository, org.mockito.Mockito.atLeastOnce())
                .saveAll(captor.capture());
        List<Long> idsConSaldo = new java.util.ArrayList<>();
        captor.getAllValues().forEach(iterable ->
                iterable.forEach(c -> {
                    if (c.getSaldoDebe().compareTo(BigDecimal.ZERO) > 0
                            || c.getSaldoHaber().compareTo(BigDecimal.ZERO) > 0) {
                        idsConSaldo.add(c.getId());
                    }
                }));

        // La cuenta secundaria (18) y TODAS sus principales (6 y 1) reciben el saldo
        assertThat(idsConSaldo).contains(18L, 6L, 1L);
        assertThat(efectivo.getSaldoDebe()).isEqualByComparingTo("500.00");
        assertThat(activo.getSaldoDebe()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("RECHAZA linea con monto en Debe y Haber a la vez")
    void rechazaLineaConAmbosMontos() {
        var request = new AsientoRequest(LocalDate.now(), "prueba",
                List.of(
                        new LineaAsientoRequest(18L, "Caja", new BigDecimal("100"), new BigDecimal("100")),
                        new LineaAsientoRequest(33L, "Venta", BigDecimal.ZERO, new BigDecimal("100"))));

        assertThatThrownBy(() -> service.registrar(request, autenticado()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Debe y Haber a la vez");
    }

    @Test
    @DisplayName("RECHAZA registrar en cuenta PRINCIPAL (solo cuentas de movimiento)")
    void rechazaCuentaPrincipal() {
        // Reemplaza el mock: la linea apunta a la cuenta principal 1.1 (id 6)
        when(cuentaRepository.findAllById(anyIterable())).thenReturn(List.of(efectivo, ventasLocales));
        var request = new AsientoRequest(LocalDate.now(), "prueba",
                List.of(
                        new LineaAsientoRequest(6L, "Caja", new BigDecimal("100"), BigDecimal.ZERO),
                        new LineaAsientoRequest(33L, "Venta", BigDecimal.ZERO, new BigDecimal("100"))));

        assertThatThrownBy(() -> service.registrar(request, autenticado()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PRINCIPAL");
    }

    @Test
    @DisplayName("Numeracion automatica: el segundo asiento lleva numero 2")
    void numeracionAutomatica() {
        when(asientoRepository.findFirstByOrderByNumeroAsientoDesc())
                .thenReturn(Optional.of(Asiento.builder().id(9L).numeroAsiento(1L).build()));

        var respuesta = service.registrar(
                asiento(new BigDecimal("100.00"), new BigDecimal("100.00")), autenticado());

        assertThat(respuesta.numeroAsiento()).isEqualTo(2L);
    }
}
