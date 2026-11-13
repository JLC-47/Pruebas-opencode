package com.example.demo.dto;

import com.example.demo.entity.Compra;
import com.example.demo.entity.EstadoCompra;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CompraResponse(
        Long id,
        Instant fecha,
        BigDecimal total,
        BigDecimal totalDevuelto,
        EstadoCompra estado,
        List<CompraItemResponse> items
) {

    public static CompraResponse from(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getFecha(),
                compra.getTotal(),
                compra.getTotalDevuelto(),
                compra.getEstado(),
                compra.getItems().stream().map(CompraItemResponse::from).toList()
        );
    }
}
