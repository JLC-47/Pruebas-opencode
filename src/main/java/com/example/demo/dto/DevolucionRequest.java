package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DevolucionRequest(

        @NotEmpty(message = "Debes indicar al menos un producto a devolver")
        @Size(max = 20, message = "Una devolucion puede tener como maximo 20 productos diferentes")
        List<@Valid ItemDevolucionRequest> items
) {
}
