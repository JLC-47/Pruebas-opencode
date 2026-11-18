package com.example.demo.exception;

public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String message) {
        super(message);
    }
}
