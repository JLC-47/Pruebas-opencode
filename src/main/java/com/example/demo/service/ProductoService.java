package com.example.demo.service;

import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Producto;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(ProductoResponse::from)
                .toList();
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

    private void aplicarDatos(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
    }
}
