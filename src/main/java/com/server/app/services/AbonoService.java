package com.server.app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.server.app.dto.finanzas.CrearAbonoDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.dto.finanzas.AbonoResponse;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.enums.EstadoCuota;
import com.server.app.entities.enums.EstadoPrestamo;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.ForbiddenException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AbonoService {

    private static final BigDecimal TASA_MORA_DIARIA = new BigDecimal("0.001");

    private final PlanPagoRepository planPagoRepository;
    private final AbonoRepository abonoRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional
    public AbonoResponse registrar(CrearAbonoDto dto, int usuarioId) {
        PlanPago cuota = planPagoRepository.findById(dto.getPlanPagoId())
                .orElseThrow(() -> new NotFoundException("Cuota (planPago) no encontrada"));

        if (cuota.getPrestamo().getUsuario().getId() != usuarioId) {
            throw new ForbiddenException("No puedes abonar a una cuota que no te pertenece");
        }

        if (cuota.getEstado() == EstadoCuota.PAGADO) {
            throw new ConfictException("La cuota #" + cuota.getNumeroCuota() + " ya está pagada");
        }

        LocalDate fechaPago = dto.getFechaPago() != null ? dto.getFechaPago() : LocalDate.now();

        BigDecimal montoCuota = cuota.getMontoCapital().add(cuota.getMontoInteres());
        BigDecimal recargoMora = calcularMora(montoCuota, cuota.getFechaVencimiento(), fechaPago);
        BigDecimal totalRequerido = montoCuota.add(recargoMora).setScale(2, RoundingMode.HALF_UP);

        if (dto.getMonto().compareTo(totalRequerido) < 0) {
            throw new BadRequestException(
                    "Monto insuficiente. Se requiere " + totalRequerido
                            + " (cuota " + montoCuota + " + mora " + recargoMora + ")");
        }

        cuota.setEstado(EstadoCuota.PAGADO);
        planPagoRepository.save(cuota);

        Abono abono = Abono.builder()
                .monto(dto.getMonto().setScale(2, RoundingMode.HALF_UP))
                .fechaPago(fechaPago)
                .recargoMora(recargoMora)
                .planPago(cuota)
                .build();
        abono = abonoRepository.save(abono);

        Prestamo prestamo = cuota.getPrestamo();
        long pendientes = planPagoRepository.countByPrestamo_IdAndEstado(prestamo.getId(), EstadoCuota.PENDIENTE);
        if (pendientes == 0) {
            prestamo.setEstado(EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
        }

        return abonoToAbonoResponse(abono);
    }

    private BigDecimal calcularMora(BigDecimal montoCuota, LocalDate vencimiento, LocalDate fechaPago) {
        if (fechaPago == null || !fechaPago.isAfter(vencimiento)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long diasAtraso = ChronoUnit.DAYS.between(vencimiento, fechaPago);
        return montoCuota
                .multiply(TASA_MORA_DIARIA)
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private AbonoResponse abonoToAbonoResponse(Abono abono) {
        return AbonoResponse.builder()
                .id(abono.getId())
                .monto(abono.getMonto())
                .fechaPago(abono.getFechaPago())
                .recargoMora(abono.getRecargoMora())
                .planPagoId(abono.getPlanPago().getId())
                .numeroCuota(abono.getPlanPago().getNumeroCuota())
                .prestamoId(abono.getPlanPago().getPrestamo().getId())
                .estadoCuota(abono.getPlanPago().getEstado())
                .estadoPrestamo(abono.getPlanPago().getPrestamo().getEstado())
                .build();
    }
}