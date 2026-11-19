package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Producto;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "nombre", "precio", "stock");
    private static final int TAMANO_MAXIMO = 100;

    private final ProductoRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(String nombre, int page, int size, String sort, String direction) {
        validarPaginacion(page, size, sort, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Producto> productos = nombre == null || nombre.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNombreContainingIgnoreCase(nombre.trim(), pageable);

        return PageResponse.from(productos.map(ProductoResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listarMisProductos(
            String username, int page, int size, String sort, String direction) {
        validarPaginacion(page, size, sort, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        return PageResponse.from(
                repository.findByVendedorUsername(username, pageable).map(ProductoResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return repository.findById(id)
                .map(ProductoResponse::from)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicarDatos(producto, request);
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request, String username, Rol rol) {
        Producto producto = new Producto();
        aplicarDatos(producto, request);
        if (rol == Rol.VENDEDOR) {
            producto.setVendedor(buscarUsuario(username));
        }
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        aplicarDatos(producto, request);
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request, String username, boolean esAdmin) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        validarPropiedad(producto, username, esAdmin);
        aplicarDatos(producto, request);
        return ProductoResponse.from(repository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public void eliminar(Long id, String username, boolean esAdmin) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        validarPropiedad(producto, username, esAdmin);
        repository.delete(producto);
    }

    private void validarPropiedad(Producto producto, String username, boolean esAdmin) {
        if (esAdmin) {
            return;
        }

        Usuario vendedor = producto.getVendedor();
        if (vendedor == null || !vendedor.getUsername().equals(username)) {
            throw new AccessDeniedException("Solo puedes gestionar los productos que tu publicaste");
        }
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNotFoundException(username));
    }

    private void validarPaginacion(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new ParametrosInvalidosException("El parametro 'page' no puede ser negativo");
        }
        if (size < 1 || size > TAMANO_MAXIMO) {
            throw new ParametrosInvalidosException(
                    "El parametro 'size' debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        if (sort == null || !CAMPOS_ORDENABLES.contains(sort)) {
            throw new ParametrosInvalidosException(
                    "El parametro 'sort' debe ser uno de: id, nombre, precio, stock");
        }
        if (direction == null
                || !("asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction))) {
            throw new ParametrosInvalidosException("El parametro 'direction' debe ser 'asc' o 'desc'");
        }
    }

    private void aplicarDatos(Producto producto, ProductoRequest request) {
        String categoria = normalizar(request.categoria());

        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);
        producto.setDescripcion(normalizar(request.descripcion()));
        producto.setImagenUrl(imagenValida(request.imagenUrl(), categoria));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String imagenValida(String imagenUrl, String categoria) {
        String imagen = normalizar(imagenUrl);
        if (imagen != null) {
            return imagen;
        }

        String nombreCategoria = categoria == null ? "general" : categoria;
        return "/img/productos/" + slug(nombreCategoria) + ".svg";
    }

    private String slug(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
