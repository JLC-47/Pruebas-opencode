package com.example.demo.dto;

import com.example.demo.entity.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductoResponseTest {

    @Test
    void fromCopiaTodosLosCamposDelProducto() {
        Producto producto = new Producto(3L, "Teclado", new BigDecimal("120000.50"), 25);

        ProductoResponse response = ProductoResponse.from(producto);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.nombre()).isEqualTo("Teclado");
        assertThat(response.precio()).isEqualByComparingTo("120000.50");
        assertThat(response.stock()).isEqualTo(25);
    }

    @Test
    void fromMantieneLosCamposNulosDelProducto() {
        Producto producto = new Producto(1L, null, null, null);

        ProductoResponse response = ProductoResponse.from(producto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isNull();
        assertThat(response.precio()).isNull();
        assertThat(response.stock()).isNull();
    }

    @Test
    void fromMantieneElIdNuloCuandoElProductoNoHaSidoGuardado() {
        Producto producto = new Producto(null, "Monitor", new BigDecimal("900000.00"), 5);

        ProductoResponse response = ProductoResponse.from(producto);

        assertThat(response.id()).isNull();
        assertThat(response.nombre()).isEqualTo("Monitor");
        assertThat(response.precio()).isEqualByComparingTo("900000.00");
        assertThat(response.stock()).isEqualTo(5);
    }
}
