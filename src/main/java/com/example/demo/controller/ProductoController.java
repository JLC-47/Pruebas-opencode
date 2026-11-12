package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Obtiene un producto por su id")
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @Operation(summary = "Crea un producto")
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    @Operation(summary = "Actualiza un producto existente")
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @Operation(summary = "Elimina un producto existente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
