package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ConfiguracionPorDefectoTest {

    @Test
    void elPerfilPorDefectoCreaElUsuarioVendedorDeDemostracion() throws IOException {
        List<PropertySource<?>> fuentes = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yaml"));

        assertThat(fuentes).hasSize(1);
        PropertySource<?> fuente = fuentes.get(0);

        assertThat(fuente.getProperty("app.seed.enabled")).isEqualTo("${APP_SEED_ENABLED:true}");
        assertThat(fuente.getProperty("app.seed.vendedor.username"))
                .isEqualTo("${APP_SEED_VENDEDOR_USERNAME:vendedor}");
        assertThat(fuente.getProperty("app.seed.vendedor.password"))
                .isEqualTo("${APP_SEED_VENDEDOR_PASSWORD:vendedor123}");
    }
}
