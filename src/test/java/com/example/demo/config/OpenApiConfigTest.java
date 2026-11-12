package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void exponeLaInformacionDeLaApi() {
        OpenAPI openAPI = new OpenApiConfig().productosOpenAPI();

        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Productos API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.0.1");
        assertThat(openAPI.getInfo().getDescription()).contains("paginacion", "busqueda", "validacion");
    }
}
