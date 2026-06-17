package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.EstadoPrestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoResponse {
    private Long id;
    private BigDecimal capitalSolicitado;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMeses;
    private EstadoPrestamo estado;
    private LocalDate fechaSolicitud;
    private BigDecimal cuotaMensual;
    private BigDecimal totalAPagar;
    private BigDecimal totalInteres;
}