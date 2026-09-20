package com.finanzassv.entity;

import com.finanzassv.enums.NaturalezaCuenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuenta del catalogo contable jerarquico (arbol autoreferencial).
 *
 * Reglas:
 * - Niveles 1 y 2: cuentas principales (control). No aceptan movimientos.
 * - Nivel 3 (hojas): cuentas secundarias (movimiento). Reciben montos parciales.
 * - Los saldos de las principales se consolidan automaticamente (mayorizacion).
 */
@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico. Su primer digito determina la clasificacion contable. */
    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_padre_id")
    private Cuenta cuentaPadre;

    @OneToMany(mappedBy = "cuentaPadre", fetch = FetchType.LAZY)
    @OrderBy("codigo ASC")
    @Builder.Default
    private List<Cuenta> subCuentas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza", length = 10, nullable = false)
    private NaturalezaCuenta naturaleza;

    /** TRUE solo para cuentas hoja (secundarias de movimiento). */
    @Column(name = "acepta_movimientos", nullable = false)
    @Builder.Default
    private Boolean aceptaMovimientos = false;

    /** Total de cargos consolidados (Debe). */
    @Column(name = "saldo_debe", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal saldoDebe = BigDecimal.ZERO;

    /** Total de abonos consolidados (Haber). */
    @Column(name = "saldo_haber", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal saldoHaber = BigDecimal.ZERO;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @PrePersist
    protected void alCrear() {
        fechaCreacion = Instant.now();
        fechaActualizacion = Instant.now();
    }

    @PreUpdate
    protected void alActualizar() {
        fechaActualizacion = Instant.now();
    }

    /** Indica si la cuenta es hoja del arbol (no tiene subcuentas registradas). */
    public boolean esHoja() {
        return subCuentas == null || subCuentas.isEmpty();
    }

    /** Saldo neto: Debe - Haber. */
    public BigDecimal saldoNeto() {
        return saldoDebe.subtract(saldoHaber);
    }
}
