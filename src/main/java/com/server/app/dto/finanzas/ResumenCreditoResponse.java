package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCreditoResponse {
    private long totalPrestamos;
    private long prestamosActivos;
    private long prestamosPagados;
    private BigDecimal capitalTotalSolicitado;
    private BigDecimal saldoPendiente;
    private BigDecimal interesPendiente;
    private long cuotasPendientes;
    private long cuotasPagadas;
    private BigDecimal totalMoraPagada;
    private LocalDate proximoVencimiento;
}