package com.server.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.EstadoPrestamo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prestamos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capital_solicitado", precision = 15, scale = 2, nullable = false)
    private BigDecimal capitalSolicitado;

    @Column(name = "tasa_interes_anual", precision = 6, scale = 2, nullable = false)
    private BigDecimal tasaInteresAnual;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPrestamo estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;
}