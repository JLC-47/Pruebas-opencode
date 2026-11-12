package com.example.demo.repository;

import com.example.demo.entity.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository repository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        repository.deleteAll();
    }

    @Test
    void guardaYRecuperaUnProducto() {
        Producto guardado = repository.save(producto("Laptop", "2500000.00", 10));

        assertThat(guardado.getId()).isNotNull();
        assertThat(repository.findById(guardado.getId()))
                .isPresent()
                .get()
                .extracting(Producto::getNombre)
                .isEqualTo("Laptop");
    }

    @Test
    void buscaPorNombreSinDistinguirMayusculas() {
        repository.saveAll(List.of(
                producto("Laptop", "2500000.00", 10),
                producto("Mouse", "45000.00", 50),
                producto("Teclado", "120000.00", 30)
        ));

        Page<Producto> resultado = repository.findByNombreContainingIgnoreCase(
                "LAP", PageRequest.of(0, 10, Sort.by("nombre")));

        assertThat(resultado.getContent())
                .extracting(Producto::getNombre)
                .containsExactly("Laptop");
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    void paginaYOrdenaPorPrecioDescendente() {
        repository.saveAll(List.of(
                producto("Laptop", "2500000.00", 10),
                producto("Mouse", "45000.00", 50),
                producto("Teclado", "120000.00", 30)
        ));

        Page<Producto> primeraPagina = repository.findAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "precio")));
        Page<Producto> segundaPagina = repository.findAll(
                PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "precio")));

        assertThat(primeraPagina.getContent())
                .extracting(Producto::getNombre)
                .containsExactly("Laptop", "Teclado");
        assertThat(primeraPagina.getTotalElements()).isEqualTo(3);
        assertThat(primeraPagina.getTotalPages()).isEqualTo(2);
        assertThat(primeraPagina.isFirst()).isTrue();
        assertThat(primeraPagina.isLast()).isFalse();
        assertThat(segundaPagina.getContent())
                .extracting(Producto::getNombre)
                .containsExactly("Mouse");
        assertThat(segundaPagina.isLast()).isTrue();
    }

    @Test
    void busquedaSinResultadosDevuelveUnaPaginaVacia() {
        repository.save(producto("Laptop", "2500000.00", 10));

        Page<Producto> resultado = repository.findByNombreContainingIgnoreCase(
                "inexistente", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }

    @Test
    void actualizaUnProductoExistente() {
        Producto guardado = repository.save(producto("Laptop", "2500000.00", 10));

        guardado.setNombre("Laptop Gamer");
        guardado.setPrecio(new BigDecimal("2300000.00"));
        guardado.setStock(8);
        repository.saveAndFlush(guardado);

        assertThat(repository.findById(guardado.getId()))
                .isPresent()
                .get()
                .satisfies(producto -> {
                    assertThat(producto.getNombre()).isEqualTo("Laptop Gamer");
                    assertThat(producto.getPrecio()).isEqualByComparingTo("2300000.00");
                    assertThat(producto.getStock()).isEqualTo(8);
                });
    }

    @Test
    void eliminaUnProductoExistente() {
        Producto guardado = repository.save(producto("Mouse", "45000.00", 50));

        repository.deleteById(guardado.getId());

        assertThat(repository.findById(guardado.getId())).isEmpty();
    }

    private Producto producto(String nombre, String precio, int stock) {
        return new Producto(null, nombre, new BigDecimal(precio), stock);
    }
}
