package com.example.demo.controller;

import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.exception.ProductoNotFoundException;
import com.example.demo.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Test
    void listarDevuelve200YElArregloDeProductos() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new ProductoResponse(1L, "Laptop", new BigDecimal("2500000.00"), 10),
                new ProductoResponse(2L, "Mouse", new BigDecimal("45000.00"), 50)
        ));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[1].nombre").value("Mouse"));
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
