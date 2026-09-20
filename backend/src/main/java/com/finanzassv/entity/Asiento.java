package com.finanzassv.entity;

import com.finanzassv.enums.EstadoAsiento;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encabezado de un asiento del Libro Diario.
 *
 * La base de datos y el servicio garantizan la Ley de la Partida Doble:
 * totalDebe debe ser EXACTAMENTE igual a totalHaber.
 */
@Entity
@Table(name = "asientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_asiento", nullable = false, unique = true)
    private Long numeroAsiento;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "concepto", length = 255, nullable = false)
    private String concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "total_debe", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalDebe = BigDecimal.ZERO;

    @Column(name = "total_haber", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalHaber = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    @Builder.Default
    private EstadoAsiento estado = EstadoAsiento.REGISTRADO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "asiento", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenLinea ASC")
    @Builder.Default
    private List<AsientoDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void alCrear() {
        fechaCreacion = Instant.now();
    }
}
