package com.example.demo.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductoRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void requestValidoNoTieneErrores() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("2500000.00"), 10));

        assertThat(violaciones).isEmpty();
    }

    @Test
    void nombreEnBlancoEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("   ", new BigDecimal("100.00"), 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("nombre");
    }

    @Test
    void nombreDeMasDe120CaracteresEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("a".repeat(121), new BigDecimal("100.00"), 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("nombre");
    }

    @Test
    void nombreDeExactamente120CaracteresEsValido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("a".repeat(120), new BigDecimal("100.00"), 1));

        assertThat(violaciones).isEmpty();
    }

    @Test
    void precioNuloEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", null, 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("precio");
    }

    @Test
    void precioNegativoEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("-0.01"), 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("precio");
    }

    @Test
    void precioConMasDeDosDecimalesEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("10.999"), 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("precio");
    }

    @Test
    void precioCeroEsValido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Muestra", new BigDecimal("0.00"), 1));

        assertThat(violaciones).isEmpty();
    }

    @Test
    void stockNuloEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("100.00"), null));

        assertThat(camposInvalidos(violaciones)).containsExactly("stock");
    }

    @Test
    void stockNegativoEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("100.00"), -1));

        assertThat(camposInvalidos(violaciones)).containsExactly("stock");
    }

    @Test
    void stockCeroEsValido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Agotado", new BigDecimal("100.00"), 0));

        assertThat(violaciones).isEmpty();
    }

    @Test
    void precioConMasDeDiezEnterosEsInvalido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("12345678901.00"), 1));

        assertThat(camposInvalidos(violaciones)).containsExactly("precio");
    }

    @Test
    void precioConDosDecimalesEsValido() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("Laptop", new BigDecimal("10.99"), 1));

        assertThat(violaciones).isEmpty();
    }

    @Test
    void variosCamposInvalidosReportanTodasLasViolaciones() {
        Set<ConstraintViolation<ProductoRequest>> violaciones = validar(
                new ProductoRequest("   ", null, null));

        assertThat(camposInvalidos(violaciones))
                .containsExactlyInAnyOrder("nombre", "precio", "stock");
    }

    private Set<ConstraintViolation<ProductoRequest>> validar(ProductoRequest request) {
        return validator.validate(request);
    }

    private String[] camposInvalidos(Set<ConstraintViolation<ProductoRequest>> violaciones) {
        return violaciones.stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .toArray(String[]::new);
    }
}
