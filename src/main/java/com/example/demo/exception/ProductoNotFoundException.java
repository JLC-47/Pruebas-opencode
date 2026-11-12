package com.example.demo.exception;

public class ProductoNotFoundException extends RuntimeException {

    public ProductoNotFoundException(Long id) {
        super("No existe un producto con id " + id);
    }
}
