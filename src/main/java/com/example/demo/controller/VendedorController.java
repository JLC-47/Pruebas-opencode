package com.example.demo.controller;

import com.example.demo.dto.ResumenVentasResponse;
import com.example.demo.service.VendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vendedor", description = "Ventas y productos del vendedor autenticado")
@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class VendedorController {

    private final VendedorService service;

    @Operation(summary = "Devuelve el resumen de ventas de los productos del vendedor")
    @GetMapping("/ventas")
    public ResumenVentasResponse ventas(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return service.obtenerVentas(username);
    }
}
