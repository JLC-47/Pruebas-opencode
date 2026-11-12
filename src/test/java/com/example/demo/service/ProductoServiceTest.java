package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Producto;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void listarDevuelveLaPaginaDeProductos() {
        Page<Producto> pagina = new PageImpl<>(
                List.of(
                        producto(1L, "Laptop", "2500000.00", 10),
                        producto(2L, "Mouse", "45000.00", 50)
                ),
                PageRequest.of(0, 2),
                2
        );
        when(repository.findAll(any(Pageable.class))).thenReturn(pagina);

        PageResponse<ProductoResponse> resultado = service.listar(null, 0, 2, "id", "asc");

        assertThat(resultado.content()).hasSize(2);
        assertThat(resultado.content().get(0).nombre()).isEqualTo("Laptop");
        assertThat(resultado.content().get(1).nombre()).isEqualTo("Mouse");
        assertThat(resultado.page()).isZero();
        assertThat(resultado.size()).isEqualTo(2);
        assertThat(resultado.totalElements()).isEqualTo(2);
        assertThat(resultado.totalPages()).isEqualTo(1);
        assertThat(resultado.first()).isTrue();
        assertThat(resultado.last()).isTrue();
    }

    @Test
    void listarBuscaPorNombreSinDistinguirMayusculasYRecortaEspacios() {
        Page<Producto> pagina = new PageImpl<>(
                List.of(producto(1L, "Laptop", "2500000.00", 10)),
                PageRequest.of(0, 10),
                1
        );
        when(repository.findByNombreContainingIgnoreCase(eq("LAP"), any(Pageable.class)))
                .thenReturn(pagina);

        PageResponse<ProductoResponse> resultado = service.listar("  LAP  ", 0, 10, "nombre", "desc");

        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.content().get(0).nombre()).isEqualTo("Laptop");
        verify(repository).findByNombreContainingIgnoreCase(eq("LAP"), any(Pageable.class));
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listarUsaFindAllCuandoElNombreEstaEnBlanco() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        PageResponse<ProductoResponse> resultado = service.listar("   ", 0, 10, "id", "asc");

        assertThat(resultado.content()).isEmpty();
        assertThat(resultado.totalElements()).isZero();
        verify(repository).findAll(any(Pageable.class));
        verify(repository, never()).findByNombreContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void listarEnviaElOrdenamientoSolicitadoAlRepositorio() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        service.listar(null, 0, 10, "precio", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("precio")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("precio").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listarAceptaLaDireccionEnMayusculas() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        service.listar(null, 0, 10, "nombre", "DESC");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("nombre").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listarAceptaElTamanoMaximoPermitido() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        PageResponse<ProductoResponse> resultado = service.listar(null, 0, 100, "id", "asc");

        assertThat(resultado.size()).isEqualTo(100);
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void listarMapeaLaSegundaPagina() {
        Page<Producto> pagina = new PageImpl<>(
                List.of(producto(3L, "Teclado", "120000.00", 30)),
                PageRequest.of(1, 2),
                3
        );
        when(repository.findAll(any(Pageable.class))).thenReturn(pagina);

        PageResponse<ProductoResponse> resultado = service.listar(null, 1, 2, "id", "asc");

        assertThat(resultado.page()).isEqualTo(1);
        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.totalPages()).isEqualTo(2);
        assertThat(resultado.first()).isFalse();
        assertThat(resultado.last()).isTrue();
    }

    @Test
    void listarRechazaPaginaNegativa() {
        assertThatThrownBy(() -> service.listar(null, -1, 10, "id", "asc"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("page");

        verifyNoInteractions(repository);
    }

    @Test
    void listarRechazaTamanoFueraDeRango() {
        assertThatThrownBy(() -> service.listar(null, 0, 0, "id", "asc"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> service.listar(null, 0, 101, "id", "asc"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("size");

        verifyNoInteractions(repository);
    }

    @Test
    void listarRechazaCampoDeOrdenamientoInvalido() {
        assertThatThrownBy(() -> service.listar(null, 0, 10, "descripcion", "asc"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("sort");

        verifyNoInteractions(repository);
    }

    @Test
    void listarRechazaDireccionInvalida() {
        assertThatThrownBy(() -> service.listar(null, 0, 10, "id", "arriba"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("direction");

        verifyNoInteractions(repository);
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
