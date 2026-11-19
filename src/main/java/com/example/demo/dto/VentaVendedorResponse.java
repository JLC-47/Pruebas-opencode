package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VentaVendedorResponse(
        Long compraId,
        Instant fecha,
        String comprador,
        String producto,
        Integer cantidad,
        BigDecimal subtotal) {
}
