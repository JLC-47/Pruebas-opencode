package com.example.demo.service;

import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Producto;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void listarDevuelveTodosLosProductos() {
        when(repository.findAll()).thenReturn(List.of(
                producto(1L, "Laptop", "2500000.00", 10),
                producto(2L, "Mouse", "45000.00", 50)
        ));

        List<ProductoResponse> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nombre()).isEqualTo("Laptop");
        assertThat(resultado.get(1).nombre()).isEqualTo("Mouse");
    }

    @Test
    void obtenerPorIdDevuelveElProductoCuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(producto(1L, "Laptop", "2500000.00", 10)));

        ProductoResponse resultado = service.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nombre()).isEqualTo("Laptop");
        assertThat(resultado.precio()).isEqualByComparingTo("2500000.00");
        assertThat(resultado.stock()).isEqualTo(10);
    }

    @Test
    void obtenerPorIdLanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crearGuardaElProductoYDevuelveLaRespuesta() {
        ProductoRequest request = new ProductoRequest("  Laptop  ", new BigDecimal("2500000.00"), 10);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        ProductoResponse resultado = service.crear(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Laptop");
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nombre()).isEqualTo("Laptop");
        assertThat(resultado.precio()).isEqualByComparingTo("2500000.00");
    }

    @Test
    void actualizarModificaElProductoCuandoExiste() {
        Producto existente = producto(1L, "Laptop", "2500000.00", 10);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ProductoResponse resultado = service.actualizar(
                1L,
                new ProductoRequest("Laptop Gamer", new BigDecimal("2300000.00"), 8)
        );

        assertThat(resultado.nombre()).isEqualTo("Laptop Gamer");
        assertThat(resultado.precio()).isEqualByComparingTo("2300000.00");
        assertThat(resultado.stock()).isEqualTo(8);
        verify(repository).save(existente);
    }

    @Test
    void actualizarLanzaExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(
                99L,
                new ProductoRequest("Nuevo", new BigDecimal("100.00"), 1)
        ))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarBorraElProductoCuandoExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminarLanzaExcepcionCuandoNoExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).deleteById(99L);
    }

    private Producto producto(Long id, String nombre, String precio, Integer stock) {
        return new Producto(id, nombre, new BigDecimal(precio), stock);
    }
}
