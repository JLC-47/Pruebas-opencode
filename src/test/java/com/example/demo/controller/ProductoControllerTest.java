package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nombre": "Laptop",
              "precio": 2500000.00,
              "stock": 10
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listarDevuelve200ConLaPaginaDeProductos() throws Exception {
        PageResponse<ProductoResponse> pagina = new PageResponse<>(
                List.of(
                        new ProductoResponse(1L, "Laptop", new BigDecimal("2500000.00"), 10),
                        new ProductoResponse(2L, "Mouse", new BigDecimal("45000.00"), 50)
                ),
                0, 10, 2, 1, true, true
        );
        when(service.listar(null, 0, 10, "id", "asc")).thenReturn(pagina);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$.content[1].nombre").value("Mouse"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void listarAceptaParametrosDePaginacionBusquedaYOrden() throws Exception {
        when(service.listar("lap", 1, 5, "nombre", "desc")).thenReturn(
                new PageResponse<>(List.of(), 1, 5, 0, 0, false, true));

        mockMvc.perform(get("/api/productos")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "nombre")
                        .param("direction", "desc")
                        .param("nombre", "lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.last").value(true));

        verify(service).listar("lap", 1, 5, "nombre", "desc");
    }

    @Test
    void listarUsaValoresPorDefectoCuandoNoSeEnvianParametros() throws Exception {
        when(service.listar(null, 0, 10, "id", "asc"))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());

        verify(service).listar(null, 0, 10, "id", "asc");
    }

    @Test
    void listarDevuelve400CuandoLosParametrosDePaginacionSonInvalidos() throws Exception {
        when(service.listar(any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new ParametrosInvalidosException("El parametro 'size' debe estar entre 1 y 100"));

        mockMvc.perform(get("/api/productos").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"))
                .andExpect(jsonPath("$.detail").value("El parametro 'size' debe estar entre 1 y 100"));
    }

    @Test
    void listarDevuelve400CuandoElParametroNoEsNumerico() throws Exception {
        mockMvc.perform(get("/api/productos").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametro invalido"));
    }

    @Test
    void obtenerDevuelve200CuandoElProductoExiste() throws Exception {
        when(service.obtenerPorId(1L))
                .thenReturn(new ProductoResponse(1L, "Laptop", new BigDecimal("2500000.00"), 10));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.precio").value(2500000.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void obtenerDevuelve404CuandoElProductoNoExiste() throws Exception {
        when(service.obtenerPorId(99L)).thenThrow(new ProductoNotFoundException(99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe un producto con id 99"));
    }

    @Test
    void crearDevuelve201ConElProductoCreado() throws Exception {
        when(service.crear(any(ProductoRequest.class)))
                .thenReturn(new ProductoResponse(5L, "Laptop", new BigDecimal("2500000.00"), 10));

        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/productos/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Laptop"));

        verify(service).crear(any(ProductoRequest.class));
    }

    @Test
    void crearDevuelve400CuandoLosDatosSonInvalidos() throws Exception {
        String jsonInvalido = """
                {
                  "nombre": "   ",
                  "precio": -1,
                  "stock": -5
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.precio").exists())
                .andExpect(jsonPath("$.errors.stock").exists());

        verify(service, never()).crear(any(ProductoRequest.class));
    }

    @Test
    void crearDevuelve400CuandoElJsonEstaMalFormado() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content("{\"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud mal formada"));
    }

    @Test
    void actualizarDevuelve200ConElProductoActualizado() throws Exception {
        when(service.actualizar(anyLong(), any(ProductoRequest.class)))
                .thenReturn(new ProductoResponse(1L, "Laptop Gamer", new BigDecimal("2300000.00"), 8));

        mockMvc.perform(put("/api/productos/1")
                        .contentType("application/json")
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Gamer"))
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void actualizarDevuelve404CuandoElProductoNoExiste() throws Exception {
        when(service.actualizar(anyLong(), any(ProductoRequest.class)))
                .thenThrow(new ProductoNotFoundException(99L));

        mockMvc.perform(put("/api/productos/99")
                        .contentType("application/json")
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDevuelve400CuandoLosDatosSonInvalidos() throws Exception {
        String jsonInvalido = """
                {
                  "nombre": "",
                  "precio": -1,
                  "stock": -1
                }
                """;

        mockMvc.perform(put("/api/productos/1")
                        .contentType("application/json")
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.precio").exists())
                .andExpect(jsonPath("$.errors.stock").exists());

        verify(service, never()).actualizar(anyLong(), any(ProductoRequest.class));
    }

    @Test
    void obtenerConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametro invalido"));
    }

    @Test
    void metodoNoSoportadoDevuelve405() throws Exception {
        mockMvc.perform(patch("/api/productos/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Metodo no permitido"));
    }

    @Test
    void eliminarDevuelve204CuandoElProductoExiste() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    void eliminarDevuelve404CuandoElProductoNoExiste() throws Exception {
        doThrow(new ProductoNotFoundException(99L)).when(service).eliminar(99L);

        mockMvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}
