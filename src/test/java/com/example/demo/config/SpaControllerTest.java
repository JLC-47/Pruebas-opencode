package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpaControllerTest {

    private final SpaController controller = new SpaController();

    @Test
    void redirigeLasRutasDelFrontendAlIndex() {
        assertThat(controller.redirigirAlFrontend()).isEqualTo("forward:/index.html");
    }
}
