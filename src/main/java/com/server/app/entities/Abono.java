package com.server.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "abonos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto", precision = 15, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "recargo_mora", precision = 15, scale = 2)
    private BigDecimal recargoMora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_pago_id", nullable = false)
    private PlanPago planPago;
}