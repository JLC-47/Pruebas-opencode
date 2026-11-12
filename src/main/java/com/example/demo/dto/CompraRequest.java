package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompraRequest(

        @NotEmpty(message = "El carrito no puede estar vacio")
        @Size(max = 20, message = "Una compra puede tener como maximo 20 productos diferentes")
        List<@Valid ItemCompraRequest> items
) {
}
