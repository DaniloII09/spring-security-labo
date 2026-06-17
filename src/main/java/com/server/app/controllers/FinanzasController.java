package com.server.app.controllers;

import java.util.List;

import com.server.app.dto.finanzas.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.User;
import com.server.app.services.AbonoService;
import com.server.app.services.PrestamoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    private final PrestamoService prestamoService;
    private final AbonoService abonoService;

    public FinanzasController(PrestamoService prestamoService, AbonoService abonoService) {
        this.prestamoService = prestamoService;
        this.abonoService = abonoService;
    }

    @GetMapping("/prestamos")
    public ResponseEntity<Pagination<PrestamoResponse>> listarPrestamos(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PrestamoResponse> p = prestamoService.listarPorUsuario(user.getId(), page, size);

        return ResponseEntity.ok(new Pagination<>(
                p.getContent(),
                new PaginationMeta(
                        p.getNumber(),
                        p.getSize(),
                        p.getTotalPages(),
                        p.getTotalElements()
                )
        ));
    }

    @PostMapping("/prestamos")
    public ResponseEntity<PrestamoResponse> solicitarPrestamo(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CrearPrestamoDto dto) {

        return ResponseEntity.ok(prestamoService.solicitar(dto, user));
    }

    @GetMapping("/prestamos/{id}/planes-pago")
    public ResponseEntity<List<PlanPagoResponse>> planesPago(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        return ResponseEntity.ok(prestamoService.planesPendientes(id, user.getId()));
    }

    @PostMapping("/abonos")
    public ResponseEntity<AbonoResponse> registrarAbono(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CrearAbonoDto dto) {

        return ResponseEntity.ok(abonoService.registrar(dto, user.getId()));
    }

    @GetMapping("/resumen-credito")
    public ResponseEntity<ResumenCreditoResponse> resumenCredito(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(prestamoService.resumen(user.getId()));
    }
}