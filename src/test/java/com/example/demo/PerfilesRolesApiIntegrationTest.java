package com.example.demo;

import com.example.demo.repository.CompraRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilesRolesApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarDatos() {
        compraRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        productoRepository.deleteAll();

        com.example.demo.entity.Usuario usuario =
                usuarioRepository.findByUsername("user").orElseThrow();
        usuario.setNombre(null);
        usuario.setBio(null);
        usuario.setAvatarUrl(null);
        usuario.setTema("claro");
        usuario.setIdioma("es");
        usuario.setNotificacionesActivas(true);
        usuarioRepository.save(usuario);
    }

    @Test
    void perfilDevuelveLosDatosDelUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/api/perfil").header("Authorization", "Bearer " + token("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.tema").value("claro"));
    }

    @Test
    void perfilRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void perfilActualizaLasPreferenciasYSeReflejaEnElLogin() throws Exception {
        String token = token("user", "user123");

        mockMvc.perform(put("/api/perfil")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "Ana Perez",
                                  "bio": "Compradora frecuente",
                                  "avatarUrl": "/avatar/ana.png",
                                  "tema": "oscuro",
                                  "idioma": "en",
                                  "notificacionesActivas": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Perez"))
                .andExpect(jsonPath("$.tema").value("oscuro"))
                .andExpect(jsonPath("$.notificacionesActivas").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                { "username": "user", "password": "user123" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombre").value("Ana Perez"))
                .andExpect(jsonPath("$.usuario.tema").value("oscuro"));

        mockMvc.perform(put("/api/perfil/password")
                        .header("Authorization", "Bearer " + token("user", "user123"))
                        .contentType("application/json")
                        .content("""
                                { "contrasenaActual": "user123", "contrasenaNueva": "nuevaClave123" }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                { "username": "user", "password": "nuevaClave123" }
                                """))
                .andExpect(status().isOk());

        com.example.demo.entity.Usuario usuario = usuarioRepository.findByUsername("user").orElseThrow();
        usuario.setPassword(passwordEncoder.encode("user123"));
        usuarioRepository.save(usuario);
    }

    @Test
    void cambioDeContrasenaRechazaLaContrasenaActualIncorrecta() throws Exception {
        mockMvc.perform(put("/api/perfil/password")
                        .header("Authorization", "Bearer " + token("user", "user123"))
                        .contentType("application/json")
                        .content("""
                                { "contrasenaActual": "incorrecta", "contrasenaNueva": "nuevaClave123" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La contrasena actual no es correcta"));
    }

    @Test
    void adminGestionaUsuariosYRoles() throws Exception {
        String admin = token("admin", "admin123");

        MvcResult creacion = mockMvc.perform(post("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "vendedor_nuevo",
                                  "password": "claveSegura123",
                                  "rol": "VENDEDOR",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andReturn();

        long id = ((Number) JsonPath.read(
                creacion.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/roles").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].nombre").value("VENDEDOR"));

        mockMvc.perform(put("/api/admin/usuarios/{id}/rol", id)
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json")
                        .content("""
                                { "rol": "USER" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"));

        mockMvc.perform(put("/api/admin/usuarios/{id}/estado", id)
                        .header("Authorization", "Bearer " + admin)
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(delete("/api/admin/usuarios/{id}", id)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioNormalNoPuedeAccederAlPanelAdministrativo() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + token("user", "user123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + token("vendedor", "vendedor123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNoPuedeCambiarSuPropioRol() throws Exception {
        Long adminId = usuarioRepository.findByUsername("admin").orElseThrow().getId();

        mockMvc.perform(put("/api/admin/usuarios/{id}/rol", adminId)
                        .header("Authorization", "Bearer " + token("admin", "admin123"))
                        .contentType("application/json")
                        .content("""
                                { "rol": "USER" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No puedes cambiar tu propio rol"));
    }

    @Test
    void vendedorGestionaSusProductosYVeSusVentas() throws Exception {
        String vendedor = token("vendedor", "vendedor123");
        String admin = token("admin", "admin123");
        String user = token("user", "user123");
        if (usuarioRepository.findByUsername("vendedor2").isEmpty()) {
            mockMvc.perform(post("/api/admin/usuarios")
                            .header("Authorization", "Bearer " + admin)
                            .contentType("application/json")
                            .content("""
                                    {
                                      "username": "vendedor2",
                                      "password": "claveSegura123",
                                      "rol": "VENDEDOR",
                                      "enabled": true
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
        String vendedor2 = token("vendedor2", "claveSegura123");

        MvcResult creacion = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + vendedor)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "Producto del vendedor",
                                  "precio": 25000.00,
                                  "stock": 5,
                                  "categoria": "Tecnologia"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long productoId = ((Number) JsonPath.read(
                creacion.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/productos/mis-productos")
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(put("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + vendedor)
                        .contentType("application/json")
                        .content("""
                                { "nombre": "Producto actualizado", "precio": 26000.00, "stock": 4 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto actualizado"));

        mockMvc.perform(put("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + vendedor2)
                        .contentType("application/json")
                        .content("""
                                { "nombre": "Intento ajeno", "precio": 1.00, "stock": 1 }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/compras")
                        .header("Authorization", "Bearer " + user)
                        .contentType("application/json")
                        .content("""
                                { "items": [ { "productoId": %d, "cantidad": 2 } ] }
                                """.formatted(productoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/vendedores/ventas")
                        .header("Authorization", "Bearer " + vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVendido").value(52000.00))
                .andExpect(jsonPath("$.unidadesVendidas").value(2))
                .andExpect(jsonPath("$.pedidos").value(1))
                .andExpect(jsonPath("$.ventas[0].producto").value("Producto actualizado"));

        mockMvc.perform(delete("/api/productos/{id}", productoId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void vendedorPuedeCrearPeroNoAccederAlPanelAdmin() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token("vendedor", "vendedor123"))
                        .contentType("application/json")
                        .content("""
                                { "nombre": "Producto vendedor", "precio": 100.00, "stock": 1 }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void vendedorPuedeIniciarSesionYAccederASusRutas() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                { "username": "vendedor", "password": "vendedor123" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.username").value("vendedor"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("VENDEDOR"))
                .andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("VENDEDOR"));

        mockMvc.perform(get("/api/productos/mis-productos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/vendedores/ventas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String token(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                { "username": "%s", "password": "%s" }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
