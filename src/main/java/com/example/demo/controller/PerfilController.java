package com.example.demo.controller;

import com.example.demo.dto.CambioContrasenaRequest;
import com.example.demo.dto.PerfilRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Perfil", description = "Consulta, edicion y personalizacion del perfil propio")
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService service;

    @Operation(summary = "Devuelve el perfil del usuario autenticado")
    @GetMapping
    public PerfilResponse obtener(Authentication authentication) {
        return service.obtener(nombreUsuario(authentication));
    }

    @Operation(summary = "Actualiza los datos y las preferencias del perfil")
    @PutMapping
    public PerfilResponse actualizar(
            Authentication authentication, @Valid @RequestBody PerfilRequest request) {
        return service.actualizar(nombreUsuario(authentication), request);
    }

    @Operation(summary = "Cambia la contrasena del usuario autenticado")
    @PutMapping("/password")
    public ResponseEntity<Void> cambiarContrasena(
            Authentication authentication, @Valid @RequestBody CambioContrasenaRequest request) {
        service.cambiarContrasena(nombreUsuario(authentication), request);
        return ResponseEntity.noContent().build();
    }

    private String nombreUsuario(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }
}
