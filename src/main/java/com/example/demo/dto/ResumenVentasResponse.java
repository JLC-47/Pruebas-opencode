package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenVentasResponse(
        BigDecimal totalVendido,
        long unidadesVendidas,
        long pedidos,
        List<VentaVendedorResponse> ventas) {
}
