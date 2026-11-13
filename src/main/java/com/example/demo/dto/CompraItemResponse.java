package com.example.demo.dto;

import com.example.demo.entity.DetalleCompra;

import java.math.BigDecimal;

public record CompraItemResponse(
        Long productoId,
        String productoNombre,
        Integer cantidad,
        Integer cantidadDevuelta,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {

    public static CompraItemResponse from(DetalleCompra detalle) {
        return new CompraItemResponse(
                detalle.getProductoId(),
                detalle.getProductoNombre(),
                detalle.getCantidad(),
                detalle.getCantidadDevueltaSegura(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
