package com.example.demo.service;

import com.example.demo.dto.ResumenVentasResponse;
import com.example.demo.dto.VentaVendedorResponse;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.repository.CompraRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendedorService {

    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public ResumenVentasResponse obtenerVentas(String username) {
        if (!usuarioRepository.existsByUsername(username)) {
            throw new UsuarioNotFoundException(username);
        }

        List<VentaVendedorResponse> ventas = compraRepository.findVentasByVendedor(username);
        BigDecimal total = ventas.stream()
                .map(VentaVendedorResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        long unidades = ventas.stream()
                .mapToLong(venta -> venta.cantidad() == null ? 0 : venta.cantidad())
                .sum();
        long pedidos = ventas.stream()
                .map(VentaVendedorResponse::compraId)
                .distinct()
                .count();

        return new ResumenVentasResponse(total, unidades, pedidos, ventas);
    }
}
