package com.example.demo.exception;

public class CompraNotFoundException extends RuntimeException {

    public CompraNotFoundException(Long id) {
        super("No existe una compra con id " + id);
    }
}
