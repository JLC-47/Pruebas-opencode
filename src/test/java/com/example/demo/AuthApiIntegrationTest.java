package com.example.demo;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Usuario;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarDatos() {
        refreshTokenRepository.deleteAll();
        productoRepository.deleteAll();
    }

    @Test
    void loginDeAdminDevuelveTokensYRoles() throws Exception {
        mockMvc.perform(login("admin", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1200))
                .andExpect(jsonPath("$.refreshExpiresIn").value(604800))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.username").value("admin"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("ADMIN"));
    }

    @Test
    void loginDeUsuarioNormalDevuelveRolUser() throws Exception {
        mockMvc.perform(login("user", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.username").value("user"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("USER"));
    }

    @Test
    void registroCreaUnClienteConRolUserYDevuelveTokens() throws Exception {
        mockMvc.perform(registro("cliente_nuevo", "claveSegura123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.username").value("cliente_nuevo"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("USER"));

        Usuario usuario = usuarioRepository.findByUsername("cliente_nuevo").orElseThrow();
        assertThat(usuario.getPassword()).isNotEqualTo("claveSegura123");
        assertThat(passwordEncoder.matches("claveSegura123", usuario.getPassword())).isTrue();

        mockMvc.perform(login("cliente_nuevo", "claveSegura123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.username").value("cliente_nuevo"));
    }

    @Test
    void registroNoPermiteElegirElRolAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "cliente_con_rol",
                                  "password": "claveSegura123",
                                  "rol": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.roles[0]").value("USER"));
    }

    @Test
    void registroDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(registro("cliente_duplicado", "claveSegura123"))
                .andExpect(status().isCreated());

        mockMvc.perform(registro("cliente_duplicado", "otraClave123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Usuario ya registrado"))
                .andExpect(jsonPath("$.detail").value(
                        "El usuario 'cliente_duplicado' ya esta registrado"));
    }

    @Test
    void registroConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(registro("ab", "corta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void credencialesInvalidasDevuelven401() throws Exception {
        mockMvc.perform(login("admin", "incorrecta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciales invalidas"))
                .andExpect(jsonPath("$.detail").value("Usuario o contrasena incorrectos"));
    }

    @Test
    void loginSinCamposDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void refreshRotaElTokenYRevocaElAnterior() throws Exception {
        MvcResult login = mockMvc.perform(login("admin", "admin123"))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

        MvcResult renovacion = mockMvc.perform(refresh(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String nuevoRefreshToken = JsonPath.read(
                renovacion.getResponse().getContentAsString(), "$.refreshToken");
        assertThat(nuevoRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(refresh(refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Token invalido"));
    }

    @Test
    void logoutRevocaElRefreshToken() throws Exception {
        MvcResult login = mockMvc.perform(login("admin", "admin123"))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType("application/json")
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(refresh(refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshExpiradoDevuelve401() throws Exception {
        MvcResult login = mockMvc.perform(login("admin", "admin123"))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        tokens.forEach(token -> token.setExpiresAt(Instant.now().minusSeconds(1)));
        refreshTokenRepository.saveAll(tokens);

        mockMvc.perform(refresh(refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Token invalido"));
    }

    @Test
    void refreshConTokenInexistenteDevuelve401() throws Exception {
        mockMvc.perform(refresh("token-que-no-existe"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Token invalido"));
    }

    @Test
    void endpointsDeProductosRequierenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));

        mockMvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNormalPuedeConsultarPeroNoEscribir() throws Exception {
        String accessToken = token("user", "user123");

        mockMvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "Laptop",
                                  "precio": 2500000.00,
                                  "stock": 10
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
    }

    @Test
    void adminPuedeEscribirProductos() throws Exception {
        String accessToken = token("admin", "admin123");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "Laptop",
                                  "precio": 2500000.00,
                                  "stock": 10
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void openApiExponeElEsquemaBearer() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registro(
            String username, String password) {
        return post("/api/auth/register")
                .contentType("application/json")
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(
            String username, String password) {
        return post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder refresh(String refreshToken) {
        return post("/api/auth/refresh")
                .contentType("application/json")
                .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken));
    }

    private String token(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(login(username, password))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
