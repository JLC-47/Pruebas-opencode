package com.example.demo.exception;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(Long id) {
        super("No existe el usuario con id " + id);
    }

    public UsuarioNotFoundException(String username) {
        super("No existe el usuario '" + username + "'");
    }
}
