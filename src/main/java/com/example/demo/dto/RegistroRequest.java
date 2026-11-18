package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "El usuario solo puede contener letras, numeros, puntos, guiones y guiones bajos")
        String username,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password
) {
}
