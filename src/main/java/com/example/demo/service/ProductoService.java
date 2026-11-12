package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Producto;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "nombre", "precio", "stock");
    private static final int TAMANO_MAXIMO = 100;

    private final ProductoRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(String nombre, int page, int size, String sort, String direction) {
        validarPaginacion(page, size, sort, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Producto> productos = nombre == null || nombre.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNombreContainingIgnoreCase(nombre.trim(), pageable);

        return PageResponse.from(productos.map(ProductoResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return repository.findById(id)
                .map(ProductoResponse::from)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicarDatos(producto, request);
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        aplicarDatos(producto, request);
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private void validarPaginacion(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new ParametrosInvalidosException("El parametro 'page' no puede ser negativo");
        }
        if (size < 1 || size > TAMANO_MAXIMO) {
            throw new ParametrosInvalidosException(
                    "El parametro 'size' debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        if (sort == null || !CAMPOS_ORDENABLES.contains(sort)) {
            throw new ParametrosInvalidosException(
                    "El parametro 'sort' debe ser uno de: id, nombre, precio, stock");
        }
        if (direction == null
                || !("asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction))) {
            throw new ParametrosInvalidosException("El parametro 'direction' debe ser 'asc' o 'desc'");
        }
    }

    private void aplicarDatos(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(normalizar(request.categoria()));
        producto.setDescripcion(normalizar(request.descripcion()));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
