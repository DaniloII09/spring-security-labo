package com.server.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    Page<Prestamo> findByUsuario_Id(int usuarioId, Pageable pageable);
    List<Prestamo> findByUsuario_Id(int usuarioId);
    Optional<Prestamo> findByIdAndUsuario_Id(Long id, int usuarioId);
}