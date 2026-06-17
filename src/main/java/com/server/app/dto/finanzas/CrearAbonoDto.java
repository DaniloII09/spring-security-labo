package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearAbonoDto {
    @NotNull(message = "El id de la cuota (planPagoId) es obligatorio")
    @Positive(message = "El planPagoId debe ser positivo")
    private Long planPagoId;

    @NotNull(message = "El monto del abono es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
}