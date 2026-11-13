package com.example.demo.service;

import com.example.demo.dto.CompraRequest;
import com.example.demo.dto.CompraResponse;
import com.example.demo.dto.DevolucionRequest;
import com.example.demo.entity.Compra;
import com.example.demo.entity.DetalleCompra;
import com.example.demo.entity.Producto;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.CompraNotFoundException;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.exception.StockInsuficienteException;
import com.example.demo.exception.TokenInvalidoException;
import com.example.demo.repository.CompraRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompraService {

    private static final int CANTIDAD_MAXIMA_POR_PRODUCTO = 99;

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CompraResponse comprar(String username, CompraRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new TokenInvalidoException("El usuario autenticado ya no existe"));

        Map<Long, Integer> cantidades = consolidarCantidades(request);
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setFecha(Instant.now());
        compra.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findByIdForUpdate(entrada.getKey())
                    .orElseThrow(() -> new ProductoNotFoundException(entrada.getKey()));

            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
            }

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario
                    .multiply(BigDecimal.valueOf(cantidad))
                    .setScale(2, RoundingMode.HALF_UP);

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);

            compra.agregarItem(new DetalleCompra(
                    producto.getId(),
                    producto.getNombre(),
                    cantidad,
                    precioUnitario,
                    subtotal));
            total = total.add(subtotal);
        }

        compra.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return CompraResponse.from(compraRepository.save(compra));
    }

    @Transactional
    public CompraResponse devolver(String username, Long compraId, DevolucionRequest request) {
        Compra compra = compraRepository.findByIdAndUsuarioUsernameForUpdate(compraId, username)
                .orElseThrow(() -> new CompraNotFoundException(compraId));

        Map<Long, Integer> cantidades = consolidarDevoluciones(request);

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Long productoId = entrada.getKey();
            int cantidad = entrada.getValue();

            DetalleCompra detalle = compra.getItems().stream()
                    .filter(item -> item.getProductoId().equals(productoId))
                    .findFirst()
                    .orElseThrow(() -> new ParametrosInvalidosException(
                            "El producto con id " + productoId + " no hace parte de esta compra"));

            int pendientes = detalle.getCantidadPendiente();
            if (pendientes <= 0) {
                throw new ParametrosInvalidosException(
                        "El producto " + detalle.getProductoNombre() + " ya fue devuelto por completo");
            }
            if (cantidad > pendientes) {
                throw new ParametrosInvalidosException(
                        "Solo puedes devolver " + pendientes + " unidad(es) de "
                                + detalle.getProductoNombre());
            }

            Producto producto = productoRepository.findByIdForUpdate(productoId)
                    .orElseThrow(() -> new ProductoNotFoundException(productoId));

            producto.setStock(producto.getStock() + cantidad);
            productoRepository.save(producto);
            detalle.setCantidadDevuelta(detalle.getCantidadDevueltaSegura() + cantidad);
        }

        return CompraResponse.from(compraRepository.save(compra));
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> listarPorUsuario(String username) {
        return compraRepository.findByUsuarioUsernameOrderByFechaDesc(username)
                .stream()
                .map(CompraResponse::from)
                .toList();
    }

    private Map<Long, Integer> consolidarCantidades(CompraRequest request) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        request.items().forEach(item ->
                cantidades.merge(item.productoId(), item.cantidad(), Integer::sum));
        validarCantidades(cantidades, "La cantidad maxima por producto es ");
        return cantidades;
    }

    private Map<Long, Integer> consolidarDevoluciones(DevolucionRequest request) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        request.items().forEach(item ->
                cantidades.merge(item.productoId(), item.cantidad(), Integer::sum));
        validarCantidades(cantidades, "La cantidad maxima a devolver por producto es ");
        return cantidades;
    }

    private void validarCantidades(Map<Long, Integer> cantidades, String mensaje) {
        cantidades.forEach((productoId, cantidad) -> {
            if (cantidad > CANTIDAD_MAXIMA_POR_PRODUCTO) {
                throw new ParametrosInvalidosException(
                        mensaje + CANTIDAD_MAXIMA_POR_PRODUCTO);
            }
        });
    }
}
