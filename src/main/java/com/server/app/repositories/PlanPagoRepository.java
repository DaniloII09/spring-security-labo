package com.server.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.PlanPago;
import com.server.app.entities.enums.EstadoCuota;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {
    List<PlanPago> findByPrestamo_IdOrderByNumeroCuotaAsc(Long prestamoId);
    List<PlanPago> findByPrestamo_IdAndEstadoOrderByNumeroCuotaAsc(Long prestamoId, EstadoCuota estado);
    List<PlanPago> findByPrestamo_Usuario_Id(int usuarioId);
    List<PlanPago> findByPrestamo_Usuario_IdAndEstado(int usuarioId, EstadoCuota estado);
    long countByPrestamo_IdAndEstado(Long prestamoId, EstadoCuota estado);
}