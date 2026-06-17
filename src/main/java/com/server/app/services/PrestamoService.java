package com.server.app.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.server.app.dto.finanzas.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.entities.enums.EstadoCuota;
import com.server.app.entities.enums.EstadoPrestamo;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AbonoRepository abonoRepository;

    @Transactional
    public PrestamoResponse solicitar(CrearPrestamoDto dto, User usuario) {
        Prestamo prestamo = Prestamo.builder()
                .capitalSolicitado(dto.getCapitalSolicitado().setScale(2, RoundingMode.HALF_UP))
                .tasaInteresAnual(dto.getTasaInteresAnual())
                .plazoMeses(dto.getPlazoMeses())
                .estado(EstadoPrestamo.APROBADO)
                .fechaSolicitud(LocalDate.now())
                .usuario(usuario)
                .build();

        prestamo = prestamoRepository.save(prestamo);

        List<PlanPago> planes = generarAmortizacion(prestamo);
        planPagoRepository.saveAll(planes);

        return toResponse(prestamo, planes);
    }

    @Transactional(readOnly = true)
    public Page<PrestamoResponse> listarPorUsuario(int usuarioId, int page, int size) {
        Page<Prestamo> prestamos = prestamoRepository.findByUsuario_Id(usuarioId, PageRequest.of(page, size));
        return prestamos.map(p -> toResponse(p,
                planPagoRepository.findByPrestamo_IdOrderByNumeroCuotaAsc(p.getId())));
    }

    @Transactional(readOnly = true)
    public List<PlanPagoResponse> planesPendientes(Long prestamoId, int usuarioId) {
        prestamoRepository.findByIdAndUsuario_Id(prestamoId, usuarioId)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado para este usuario"));

        return planPagoRepository
                .findByPrestamo_IdAndEstadoOrderByNumeroCuotaAsc(prestamoId, EstadoCuota.PENDIENTE)
                .stream()
                .map(this::planToPlanPagoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenCreditoResponse resumen(int usuarioId) {
        List<Prestamo> prestamos = prestamoRepository.findByUsuario_Id(usuarioId);

        long activos = prestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.APROBADO).count();
        long pagados = prestamos.stream().filter(p -> p.getEstado() == EstadoPrestamo.PAGADO).count();

        BigDecimal capitalTotal = prestamos.stream()
                .map(Prestamo::getCapitalSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<PlanPago> pendientes =
                planPagoRepository.findByPrestamo_Usuario_IdAndEstado(usuarioId, EstadoCuota.PENDIENTE);

        BigDecimal saldoPendiente = pendientes.stream()
                .map(PlanPago::getMontoCapital)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal interesPendiente = pendientes.stream()
                .map(PlanPago::getMontoInteres)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long cuotasPagadas =
                planPagoRepository.findByPrestamo_Usuario_IdAndEstado(usuarioId, EstadoCuota.PAGADO).size();

        BigDecimal totalMora = abonoRepository.findByPlanPago_Prestamo_Usuario_Id(usuarioId).stream()
                .map(a -> a.getRecargoMora() == null ? BigDecimal.ZERO : a.getRecargoMora())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate proximoVencimiento = pendientes.stream()
                .map(PlanPago::getFechaVencimiento)
                .min(LocalDate::compareTo)
                .orElse(null);

        return ResumenCreditoResponse.builder()
                .totalPrestamos(prestamos.size())
                .prestamosActivos(activos)
                .prestamosPagados(pagados)
                .capitalTotalSolicitado(capitalTotal)
                .saldoPendiente(saldoPendiente)
                .interesPendiente(interesPendiente)
                .cuotasPendientes(pendientes.size())
                .cuotasPagadas(cuotasPagadas)
                .totalMoraPagada(totalMora)
                .proximoVencimiento(proximoVencimiento)
                .build();
    }

    private List<PlanPago> generarAmortizacion(Prestamo prestamo) {
        BigDecimal capital = prestamo.getCapitalSolicitado().setScale(2, RoundingMode.HALF_UP);
        int n = prestamo.getPlazoMeses();

        BigDecimal iMensual = prestamo.getTasaInteresAnual()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal capitalPorCuota = capital.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        List<PlanPago> planes = new ArrayList<>();
        BigDecimal saldo = capital;

        for (int k = 1; k <= n; k++) {
            BigDecimal interes = saldo.multiply(iMensual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = capitalPorCuota;

            saldo = saldo.subtract(principal);

            planes.add(PlanPago.builder()
                    .numeroCuota(k)
                    .montoCapital(principal)
                    .montoInteres(interes)
                    .fechaVencimiento(prestamo.getFechaSolicitud().plusMonths(k))
                    .estado(EstadoCuota.PENDIENTE)
                    .prestamo(prestamo)
                    .build());
        }
        return planes;
    }

    private PrestamoResponse toResponse(Prestamo p, List<PlanPago> planes) {
        BigDecimal totalCapital = planes.stream()
                .map(PlanPago::getMontoCapital)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalInteres = planes.stream()
                .map(PlanPago::getMontoInteres)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal cuotaMensual = planes.isEmpty()
                ? BigDecimal.ZERO
                : planes.get(0).getMontoCapital().add(planes.get(0).getMontoInteres());

        return PrestamoResponse.builder()
                .id(p.getId())
                .capitalSolicitado(p.getCapitalSolicitado())
                .tasaInteresAnual(p.getTasaInteresAnual())
                .plazoMeses(p.getPlazoMeses())
                .estado(p.getEstado())
                .fechaSolicitud(p.getFechaSolicitud())
                .cuotaMensual(cuotaMensual)
                .totalAPagar(totalCapital.add(totalInteres))
                .totalInteres(totalInteres)
                .build();
    }

    private PlanPagoResponse planToPlanPagoResponse(PlanPago plan) {
        BigDecimal montoCuota = plan.getMontoCapital().add(plan.getMontoInteres());
        return PlanPagoResponse.builder()
                .id(plan.getId())
                .numeroCuota(plan.getNumeroCuota())
                .montoCuota(montoCuota)
                .montoCapital(plan.getMontoCapital())
                .montoInteres(plan.getMontoInteres())
                .fechaVencimiento(plan.getFechaVencimiento())
                .estado(plan.getEstado())
                .build();
    }
}