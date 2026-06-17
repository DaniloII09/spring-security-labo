package com.server.app.dto.finanzas;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearPrestamoDto {
    @Positive(message = "Capital solicitado debe ser mayor a 0")
    private BigDecimal capitalSolicitado;

    @DecimalMin(value = "0", message = "Tasa de interés no puede ser negativa")
    private BigDecimal tasaInteresAnual;

    @Positive(message = "Plazo debe estar entre 1 y 600 meses")
    @Max(value = 600, message = "Plazo máximo de 600 meses")
    private Integer plazoMeses;
}