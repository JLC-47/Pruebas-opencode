package com.example.demo.controller;

import com.example.demo.dto.RolRequest;
import com.example.demo.dto.RolResponse;
import com.example.demo.dto.UsuarioAdminRequest;
import com.example.demo.dto.UsuarioAdminResponse;
import com.example.demo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Administracion", description = "Gestion de usuarios y roles del sistema")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @Operation(summary = "Lista todos los usuarios registrados")
    @GetMapping("/usuarios")
    public List<UsuarioAdminResponse> listarUsuarios() {
        return service.listar();
    }

    @Operation(summary = "Obtiene un usuario por su id")
    @GetMapping("/usuarios/{id}")
    public UsuarioAdminResponse obtenerUsuario(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crea un usuario con el rol indicado")
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioAdminResponse> crearUsuario(
            @Valid @RequestBody UsuarioAdminRequest request) {
        UsuarioAdminResponse creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/admin/usuarios/" + creado.id())).body(creado);
    }

    @Operation(summary = "Actualiza los datos, la contrasena, el rol y el estado de un usuario")
    @PutMapping("/usuarios/{id}")
    public UsuarioAdminResponse actualizarUsuario(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAdminRequest request) {
        return service.actualizar(id, request, nombreUsuario(authentication));
    }

    @Operation(summary = "Elimina un usuario")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(Authentication authentication, @PathVariable Long id) {
        service.eliminar(id, nombreUsuario(authentication));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Asigna un rol a un usuario")
    @PutMapping("/usuarios/{id}/rol")
    public UsuarioAdminResponse cambiarRol(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request) {
        return service.cambiarRol(id, request, nombreUsuario(authentication));
    }

    @Operation(summary = "Activa o desactiva un usuario")
    @PutMapping("/usuarios/{id}/estado")
    public UsuarioAdminResponse cambiarEstado(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return service.cambiarEstado(id, enabled, nombreUsuario(authentication));
    }

    @Operation(summary = "Lista los roles disponibles y sus permisos")
    @GetMapping("/roles")
    public List<RolResponse> listarRoles() {
        return service.roles();
    }

    private String nombreUsuario(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }
}
