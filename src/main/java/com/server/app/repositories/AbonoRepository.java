package com.server.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.Abono;

public interface AbonoRepository extends JpaRepository<Abono, Long> {
    List<Abono> findByPlanPago_Prestamo_Usuario_Id(int usuarioId);
}