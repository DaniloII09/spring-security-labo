package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.EstadoCuota;
import com.server.app.entities.enums.EstadoPrestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonoResponse {
    private Long id;
    private BigDecimal monto;
    private LocalDate fechaPago;
    private BigDecimal recargoMora;
    private Long planPagoId;
    private Integer numeroCuota;
    private Long prestamoId;
    private EstadoCuota estadoCuota;
    private EstadoPrestamo estadoPrestamo;
}