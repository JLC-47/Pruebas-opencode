package com.example.demo.controller;

import com.example.demo.dto.CompraRequest;
import com.example.demo.dto.CompraResponse;
import com.example.demo.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Compras", description = "Checkout e historial de compras del usuario autenticado")
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService service;

    @Operation(summary = "Registra la compra del carrito y descuenta el stock")
    @PostMapping
    public ResponseEntity<CompraResponse> comprar(
            Authentication authentication,
            @Valid @RequestBody CompraRequest request) {
        CompraResponse compra = service.comprar(authentication.getName(), request);
        return ResponseEntity.created(URI.create("/api/compras/" + compra.id())).body(compra);
    }

    @Operation(summary = "Lista las compras del usuario autenticado")
    @GetMapping
    public List<CompraResponse> listar(Authentication authentication) {
        return service.listarPorUsuario(authentication.getName());
    }
}
