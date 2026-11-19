package com.example.demo.service;

import com.example.demo.dto.ResumenVentasResponse;
import com.example.demo.dto.VentaVendedorResponse;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.repository.CompraRepository;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendedorServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VendedorService service;

    @Test
    void obtenerVentasCalculaElResumen() {
        Instant fecha = Instant.parse("2026-01-01T10:00:00Z");
        List<VentaVendedorResponse> ventas = List.of(
                new VentaVendedorResponse(1L, fecha, "cliente1", "Laptop", 2, new BigDecimal("100.00")),
                new VentaVendedorResponse(1L, fecha, "cliente1", "Mouse", 3, new BigDecimal("30.00")),
                new VentaVendedorResponse(2L, fecha, "cliente2", "Laptop", 1, new BigDecimal("50.00")));

        when(usuarioRepository.existsByUsername("vendedor")).thenReturn(true);
        when(compraRepository.findVentasByVendedor("vendedor")).thenReturn(ventas);

        ResumenVentasResponse resumen = service.obtenerVentas("vendedor");

        assertThat(resumen.totalVendido()).isEqualByComparingTo("180.00");
        assertThat(resumen.unidadesVendidas()).isEqualTo(6);
        assertThat(resumen.pedidos()).isEqualTo(2);
        assertThat(resumen.ventas()).hasSize(3);
    }

    @Test
    void obtenerVentasDevuelveCerosCuandoNoHayVentas() {
        when(usuarioRepository.existsByUsername("vendedor")).thenReturn(true);
        when(compraRepository.findVentasByVendedor("vendedor")).thenReturn(List.of());

        ResumenVentasResponse resumen = service.obtenerVentas("vendedor");

        assertThat(resumen.totalVendido()).isEqualByComparingTo("0.00");
        assertThat(resumen.unidadesVendidas()).isZero();
        assertThat(resumen.pedidos()).isZero();
    }

    @Test
    void obtenerVentasLanzaExcepcionCuandoElVendedorNoExiste() {
        when(usuarioRepository.existsByUsername("nadie")).thenReturn(false);

        assertThatThrownBy(() -> service.obtenerVentas("nadie"))
                .isInstanceOf(UsuarioNotFoundException.class);

        verifyNoInteractions(compraRepository);
    }
}
