package com.example.demo.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void productoNoEncontradoDevuelve404() {
        ProblemDetail problem = handler.handleProductoNotFound(new ProductoNotFoundException(7L));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Producto no encontrado");
        assertThat(problem.getDetail()).isEqualTo("No existe un producto con id 7");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void parametrosInvalidosDevuelven400ConElDetalle() {
        ProblemDetail problem = handler.handleParametrosInvalidos(
                new ParametrosInvalidosException("El parametro 'size' debe estar entre 1 y 100"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Parametros invalidos");
        assertThat(problem.getDetail()).isEqualTo("El parametro 'size' debe estar entre 1 y 100");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void validacionDevuelve400ConLosErroresPorCampo() throws Exception {
        MethodArgumentNotValidException ex = validacionInvalida(
                new FieldError("productoRequest", "nombre", "El nombre es obligatorio"),
                new FieldError("productoRequest", "precio", "El precio no puede ser negativo")
        );

        ProblemDetail problem = handler.handleValidationErrors(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Error de validacion");
        assertThat(problem.getDetail()).isEqualTo("La solicitud contiene datos invalidos");
        Map<String, String> errors = errores(problem);
        assertThat(errors)
                .hasSize(2)
                .containsEntry("nombre", "El nombre es obligatorio")
                .containsEntry("precio", "El precio no puede ser negativo");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void validacionConservaElPrimerErrorDeCadaCampo() throws Exception {
        MethodArgumentNotValidException ex = validacionInvalida(
                new FieldError("productoRequest", "precio", "El precio es obligatorio"),
                new FieldError("productoRequest", "precio", "El precio no puede ser negativo")
        );

        Map<String, String> errors = errores(handler.handleValidationErrors(ex));

        assertThat(errors).hasSize(1);
        assertThat(errors).containsEntry("precio", "El precio es obligatorio");
    }

    @Test
    void validacionSinErroresDevuelveUnMapaVacio() throws Exception {
        Map<String, String> errors = errores(handler.handleValidationErrors(validacionInvalida()));

        assertThat(errors).isEmpty();
    }

    @Test
    void cuerpoIlegibleDevuelve400() {
        ProblemDetail problem = handler.handleUnreadableBody(
                new HttpMessageNotReadableException("json invalido", (HttpInputMessage) null));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Solicitud mal formada");
        assertThat(problem.getDetail()).isEqualTo("El cuerpo de la solicitud no es un JSON valido");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void tipoDeParametroInvalidoDevuelve400ConElNombreDelParametro() {
        ProblemDetail problem = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Parametro invalido");
        assertThat(problem.getDetail()).isEqualTo("El parametro 'id' tiene un formato invalido");
    }

    @Test
    void recursoNoEncontradoDevuelve404() {
        ProblemDetail problem = handler.handleNoResource(
                new NoResourceFoundException(HttpMethod.GET, "/api/desconocido", "No static resource"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Recurso no encontrado");
        assertThat(problem.getDetail()).isEqualTo("No existe el recurso solicitado");
    }

    @Test
    void metodoNoSoportadoDevuelve405ConElMetodoSolicitado() {
        ProblemDetail problem = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PATCH"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(problem.getTitle()).isEqualTo("Metodo no permitido");
        assertThat(problem.getDetail()).isEqualTo("El metodo HTTP PATCH no esta permitido para esta ruta");
    }

    @Test
    void conflictoDeIntegridadDevuelve409() {
        ProblemDetail problem = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicado"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Conflicto de datos");
        assertThat(problem.getDetail()).isEqualTo(
                "No fue posible guardar el producto por un conflicto con los datos existentes");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void errorInesperadoDevuelve500() {
        ProblemDetail problem = handler.handleUnexpectedError(new RuntimeException("boom"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Error interno");
        assertThat(problem.getDetail()).isEqualTo(
                "Ocurrio un error inesperado. Intenta nuevamente mas tarde");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    private MethodArgumentNotValidException validacionInvalida(FieldError... errores) throws Exception {
        MethodParameter parametro = new MethodParameter(
                ControladorFalso.class.getDeclaredMethod("crear", Object.class), 0);
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "productoRequest");
        for (FieldError error : errores) {
            resultado.addError(error);
        }
        return new MethodArgumentNotValidException(parametro, resultado);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> errores(ProblemDetail problem) {
        return (Map<String, String>) problem.getProperties().get("errors");
    }

    private static class ControladorFalso {
        @SuppressWarnings("unused")
        void crear(Object body) {
        }
    }
}
