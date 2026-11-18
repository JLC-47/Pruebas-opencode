package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SpaControllerTest {

    private final SpaController controller = new SpaController();

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void redirigeLasRutasDelFrontendAlIndex() {
        assertThat(controller.redirigirAlFrontend()).isEqualTo("forward:/index.html");
    }

    @Test
    void lasRutasDelFrontendSeReenvianAlIndex() throws Exception {
        for (String ruta : new String[] {
            "/",
            "/login",
            "/registro",
            "/productos",
            "/productos/nuevo",
            "/productos/5/editar",
            "/carrito",
            "/mis-compras"
        }) {
            mockMvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/index.html"));
        }
    }
}
