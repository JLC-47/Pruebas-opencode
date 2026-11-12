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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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

    private String accessToken;

    @BeforeEach
    void prepararBaseDeDatosYSesion() throws Exception {
        repository.deleteAll();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
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
                        .with(autenticado())
                        .contentType("application/json")
                        .content(jsonCreacion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.precio").value(2500000.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andReturn();

        long id = ((Number) JsonPath.read(creacion.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/productos").with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/productos/{id}", id).with(autenticado()))
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
                        .with(autenticado())
                        .contentType("application/json")
                        .content(jsonActualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Gamer"))
                .andExpect(jsonPath("$.precio").value(2300000.00))
                .andExpect(jsonPath("$.stock").value(8));

        mockMvc.perform(delete("/api/productos/{id}", id).with(autenticado()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/{id}", id).with(autenticado()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/productos").with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void paginacionYBusquedaFuncionan() throws Exception {
        crearProducto("Laptop", "2500000.00", 10);
        crearProducto("Mouse", "45000.00", 50);
        crearProducto("Teclado", "120000.00", 30);

        mockMvc.perform(get("/api/productos")
                        .with(autenticado())
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "precio")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$.content[1].nombre").value("Teclado"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/productos")
                        .with(autenticado())
                        .param("nombre", "LAP")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void laSegundaPaginaDevuelveLosMetadatosCorrectos() throws Exception {
        crearProducto("Laptop", "2500000.00", 10);
        crearProducto("Mouse", "45000.00", 50);
        crearProducto("Teclado", "120000.00", 30);

        mockMvc.perform(get("/api/productos")
                        .with(autenticado())
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void healthEndpointDevuelveUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void documentacionOpenApiEstaDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Productos API"))
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    void rutaDesconocidaDevuelve404ConProblemDetail() throws Exception {
        mockMvc.perform(get("/api/desconocido").with(autenticado()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }

    @Test
    void parametrosDePaginacionInvalidosDevuelven400() throws Exception {
        mockMvc.perform(get("/api/productos").with(autenticado()).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"));

        mockMvc.perform(get("/api/productos").with(autenticado()).param("sort", "descripcion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"));

        mockMvc.perform(get("/api/productos").with(autenticado()).param("direction", "arriba"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"));
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
                        .with(autenticado())
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
        mockMvc.perform(get("/api/productos/999999").with(autenticado()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
    }

    private RequestPostProcessor autenticado() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + accessToken);
            return request;
        };
    }

    private void crearProducto(String nombre, String precio, int stock) throws Exception {
        String json = """
                {
                  "nombre": "%s",
                  "precio": %s,
                  "stock": %d
                }
                """.formatted(nombre, precio, stock);

        mockMvc.perform(post("/api/productos")
                        .with(autenticado())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());
    }
}
