package com.example.demo;

import com.example.demo.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository repository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        repository.deleteAll();
    }

    @Test
    void flujoCrudCompleto() throws Exception {
        String jsonCreacion = """
                {
                  "nombre": "Laptop",
                  "precio": 2500000.00,
                  "stock": 10
                }
                """;

        MvcResult creacion = mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(jsonCreacion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.precio").value(2500000.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andReturn();

        long id = ((Number) JsonPath.read(creacion.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Laptop"));

        String jsonActualizacion = """
                {
                  "nombre": "Laptop Gamer",
                  "precio": 2300000.00,
                  "stock": 8
                }
                """;

        mockMvc.perform(put("/api/productos/{id}", id)
                        .contentType("application/json")
                        .content(jsonActualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Gamer"))
                .andExpect(jsonPath("$.precio").value(2300000.00))
                .andExpect(jsonPath("$.stock").value(8));

        mockMvc.perform(delete("/api/productos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void crearProductoInvalidoDevuelve400ConLosErrores() throws Exception {
        String jsonInvalido = """
                {
                  "nombre": "",
                  "precio": -100.00,
                  "stock": -1
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
    }

    @Test
    void obtenerProductoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/productos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
    }
}
