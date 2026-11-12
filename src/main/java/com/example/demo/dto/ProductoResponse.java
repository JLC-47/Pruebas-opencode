package com.example.demo.dto;

import com.example.demo.entity.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock,
        String categoria,
        String descripcion
) {

    public ProductoResponse(Long id, String nombre, BigDecimal precio, Integer stock) {
        this(id, nombre, precio, stock, null, null);
    }

    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria(),
                producto.getDescripcion()
        );
    }
}
