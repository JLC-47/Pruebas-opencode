package com.example.demo.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String producto, int disponible) {
        super("No hay stock suficiente para " + producto + ". Disponible: " + disponible);
    }
}
