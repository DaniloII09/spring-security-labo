package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.EstadoCuota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPagoResponse {
    private Long id;
    private Integer numeroCuota;
    private BigDecimal montoCuota;
    private BigDecimal montoCapital;
    private BigDecimal montoInteres;
    private LocalDate fechaVencimiento;
    private EstadoCuota estado;
}