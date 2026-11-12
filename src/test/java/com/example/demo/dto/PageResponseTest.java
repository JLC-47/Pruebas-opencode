package com.example.demo.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void fromCopiaLaInformacionDeLaPagina() {
        PageImpl<String> pagina = new PageImpl<>(
                List.of("Laptop", "Teclado"),
                PageRequest.of(0, 2),
                5
        );

        PageResponse<String> response = PageResponse.from(pagina);

        assertThat(response.content()).containsExactly("Laptop", "Teclado");
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse();
    }

    @Test
    void fromReflejaUnaPaginaIntermedia() {
        PageImpl<String> pagina = new PageImpl<>(
                List.of("Mouse"),
                PageRequest.of(1, 2),
                3
        );

        PageResponse<String> response = PageResponse.from(pagina);

        assertThat(response.content()).containsExactly("Mouse");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();
    }

    @Test
    void fromReflejaUnaPaginaVaciaComoPrimeraYUltima() {
        PageImpl<String> pagina = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.from(pagina);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }
}
