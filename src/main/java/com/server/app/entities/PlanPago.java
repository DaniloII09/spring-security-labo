package com.server.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.EstadoCuota;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "planes_pago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "monto_capital", precision = 15, scale = 2, nullable = false)
    private BigDecimal montoCapital;

    @Column(name = "monto_interes", precision = 15, scale = 2, nullable = false)
    private BigDecimal montoInteres;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCuota estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;
}