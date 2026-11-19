package com.example.demo.dto;

import com.example.demo.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioAdminRequest(

        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "El usuario solo puede contener letras, numeros, puntos, guiones y guiones bajos")
        String username,

        @Size(max = 72, message = "La contrasena no puede superar los 72 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        Boolean enabled
) {
}
