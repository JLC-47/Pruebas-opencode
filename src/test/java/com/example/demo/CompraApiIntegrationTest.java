package com.example.demo;

import com.example.demo.repository.CompraRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompraApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void prepararDatos() throws Exception {
        compraRepository.deleteAll();
        productoRepository.deleteAll();
        adminToken = token("admin", "admin123");
        userToken = token("user", "user123");
    }

    @Test
    void usuarioCompraYSeDescuentaElStock() throws Exception {
        long productoId = crearProducto("Laptop", "2500000.00", 5);

        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(compraJson(productoId, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.total").value(5000000.00))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productoId").value(productoId))
                .andExpect(jsonPath("$.items[0].productoNombre").value("Laptop"))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(5000000.00));

        mockMvc.perform(get("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void compraSinStockDevuelve409YNoModificaElProducto() throws Exception {
        long productoId = crearProducto("Monitor", "900000.00", 1);

        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(compraJson(productoId, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Stock insuficiente"))
                .andExpect(jsonPath("$.detail").value(
                        "No hay stock suficiente para Monitor. Disponible: 1"));

        mockMvc.perform(get("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.stock").value(1));
    }

    @Test
    void compraVaciaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void cantidadMayorAlMaximoDevuelve400() throws Exception {
        long productoId = crearProducto("Teclado", "120000.00", 200);

        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(compraJson(productoId, 100)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].cantidad']").exists());
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(compraJson(999999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
    }

    @Test
    void productosDuplicadosSeConsolidanEnUnaSolaLinea() throws Exception {
        long productoId = crearProducto("Mouse", "45000.00", 10);

        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "items": [
                                    { "productoId": %d, "cantidad": 1 },
                                    { "productoId": %d, "cantidad": 2 }
                                  ]
                                }
                                """.formatted(productoId, productoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].cantidad").value(3))
                .andExpect(jsonPath("$.total").value(135000.00));
    }

    @Test
    void historialSoloMuestraLasComprasDelUsuarioAutenticado() throws Exception {
        long laptopId = crearProducto("Laptop", "2500000.00", 5);
        long mouseId = crearProducto("Mouse", "45000.00", 5);

        comprar(userToken, compraJson(laptopId, 1));
        comprar(adminToken, compraJson(mouseId, 2));

        mockMvc.perform(get("/api/compras")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].productoNombre").value("Laptop"));

        mockMvc.perform(get("/api/compras")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].productoNombre").value("Mouse"));
    }

    @Test
    void compraRequiereAutenticacion() throws Exception {
        mockMvc.perform(post("/api/compras")
                        .contentType("application/json")
                        .content(compraJson(1L, 1)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void devolucionTotalReponeElInventarioYCambiaElEstado() throws Exception {
        long productoId = crearProducto("Laptop", "2500000.00", 5);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 3));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA_TOTAL"))
                .andExpect(jsonPath("$.totalDevuelto").value(7500000.00))
                .andExpect(jsonPath("$.items[0].cantidadDevuelta").value(3));

        assertStock(productoId, 5);
    }

    @Test
    void devolucionParcialReponeSoloLasUnidadesDevueltas() throws Exception {
        long productoId = crearProducto("Monitor", "2000000.00", 10);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 4));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA_PARCIAL"))
                .andExpect(jsonPath("$.totalDevuelto").value(4000000.00))
                .andExpect(jsonPath("$.items[0].cantidadDevuelta").value(2));

        assertStock(productoId, 8);

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA_TOTAL"))
                .andExpect(jsonPath("$.items[0].cantidadDevuelta").value(4));

        assertStock(productoId, 10);
    }

    @Test
    void devolucionMayorALoCompradoDevuelve400YNoModificaElStock() throws Exception {
        long productoId = crearProducto("Teclado", "120000.00", 10);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 2));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"))
                .andExpect(jsonPath("$.detail").value(
                        "Solo puedes devolver 2 unidad(es) de Teclado"));

        assertStock(productoId, 8);

        mockMvc.perform(get("/api/compras")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].cantidadDevuelta").value(0));
    }

    @Test
    void noSePuedeDevolverUnaCompraDeOtroUsuario() throws Exception {
        long productoId = crearProducto("Mouse", "45000.00", 6);
        long compraId = comprarYObtenerId(adminToken, compraJson(productoId, 2));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Compra no encontrada"));

        assertStock(productoId, 4);
    }

    @Test
    void devolucionDeUnProductoQueNoEstaEnLaCompraDevuelve400() throws Exception {
        long compradoId = crearProducto("Laptop", "2500000.00", 5);
        long otroId = crearProducto("Audifonos", "399900.00", 5);
        long compraId = comprarYObtenerId(userToken, compraJson(compradoId, 1));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(otroId, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parametros invalidos"))
                .andExpect(jsonPath("$.detail").value(
                        "El producto con id " + otroId + " no hace parte de esta compra"));

        assertStock(compradoId, 4);
        assertStock(otroId, 5);
    }

    @Test
    void noSePuedeDevolverDosVecesElMismoProducto() throws Exception {
        long productoId = crearProducto("Base para Laptop", "79900.00", 3);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 1));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "El producto Base para Laptop ya fue devuelto por completo"));

        assertStock(productoId, 3);
    }

    @Test
    void devolucionConsolidaCantidadesDuplicadas() throws Exception {
        long productoId = crearProducto("Hub USB-C", "159900.00", 5);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 2));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "items": [
                                    { "productoId": %d, "cantidad": 1 },
                                    { "productoId": %d, "cantidad": 1 }
                                  ]
                                }
                                """.formatted(productoId, productoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA_TOTAL"))
                .andExpect(jsonPath("$.items[0].cantidadDevuelta").value(2));

        assertStock(productoId, 5);
    }

    @Test
    void devolucionRequiereAutenticacion() throws Exception {
        long productoId = crearProducto("Camara Web", "219900.00", 5);
        long compraId = comprarYObtenerId(userToken, compraJson(productoId, 1));

        mockMvc.perform(post("/api/compras/{id}/devoluciones", compraId)
                        .contentType("application/json")
                        .content(devolucionJson(productoId, 1)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    private long crearProducto(String nombre, String precio, int stock) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "%s",
                                  "precio": %s,
                                  "stock": %d
                                }
                                """.formatted(nombre, precio, stock)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String compraJson(long productoId, int cantidad) {
        return """
                {
                  "items": [
                    { "productoId": %d, "cantidad": %d }
                  ]
                }
                """.formatted(productoId, cantidad);
    }

    private void comprar(String token, String json) throws Exception {
        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());
    }

    private long comprarYObtenerId(String token, String json) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String devolucionJson(long productoId, int cantidad) {
        return """
                {
                  "items": [
                    { "productoId": %d, "cantidad": %d }
                  ]
                }
                """.formatted(productoId, cantidad);
    }

    private void assertStock(long productoId, int esperado) throws Exception {
        mockMvc.perform(get("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(esperado));
    }

    private String token(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
