package com.finanzassv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Linea Debe/Haber de un asiento del Libro Diario.
 *
 * Cada linea mueve monto en EXACTAMENTE una de las dos columnas
 * (restriccion chk_detalles_linea_exclusiva en la base de datos).
 */
@Entity
@Table(name = "asiento_detalles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsientoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento;

    /** Solo cuentas de movimiento (aceptaMovimientos = TRUE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "concepto", length = 255, nullable = false)
    private String concepto;

    @Column(name = "monto_debe", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoDebe = BigDecimal.ZERO;

    @Column(name = "monto_haber", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoHaber = BigDecimal.ZERO;

    @Column(name = "orden_linea", nullable = false)
    @Builder.Default
    private Integer ordenLinea = 0;
}
