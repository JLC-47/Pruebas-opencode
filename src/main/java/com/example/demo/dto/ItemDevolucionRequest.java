package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemDevolucionRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad a devolver debe ser al menos 1")
        @Max(value = 99, message = "La cantidad maxima por producto es 99")
        Integer cantidad
) {
}
