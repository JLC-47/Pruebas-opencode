package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.security.Autoridades;
import com.example.demo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Productos", description = "Operaciones sobre el catalogo de productos")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @Operation(summary = "Lista productos con paginacion y busqueda por nombre")
    @GetMapping
    public PageResponse<ProductoResponse> listar(
            @Parameter(description = "Numero de pagina, inicia en 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Cantidad de elementos por pagina, entre 1 y 100")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo de ordenamiento: id, nombre, precio o stock")
            @RequestParam(defaultValue = "id") String sort,

            @Parameter(description = "Direccion del ordenamiento: asc o desc")
            @RequestParam(defaultValue = "asc") String direction,

            @Parameter(description = "Filtro opcional por nombre, sin distinguir mayusculas")
            @RequestParam(required = false) String nombre) {
        return service.listar(nombre, page, size, sort, direction);
    }

    @Operation(summary = "Lista los productos publicados por el vendedor autenticado")
    @GetMapping("/mis-productos")
    public PageResponse<ProductoResponse> listarMisProductos(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.listarMisProductos(
                nombreUsuario(authentication), page, size, sort, direction);
    }

    @Operation(summary = "Obtiene un producto por su id")
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @Operation(summary = "Crea un producto; si el usuario es vendedor queda asociado a el")
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            Authentication authentication,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = service.crear(
                request, nombreUsuario(authentication), rol(authentication));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    @Operation(summary = "Actualiza un producto propio o cualquier producto si es administrador")
    @PutMapping("/{id}")
    public ProductoResponse actualizar(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(
                id, request, nombreUsuario(authentication), esAdmin(authentication));
    }

    @Operation(summary = "Elimina un producto propio o cualquier producto si es administrador")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(Authentication authentication, @PathVariable Long id) {
        service.eliminar(id, nombreUsuario(authentication), esAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private String nombreUsuario(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication != null && Autoridades.esAdmin(authentication);
    }

    private com.example.demo.entity.Rol rol(Authentication authentication) {
        return authentication == null ? com.example.demo.entity.Rol.USER : Autoridades.rol(authentication);
    }
}
