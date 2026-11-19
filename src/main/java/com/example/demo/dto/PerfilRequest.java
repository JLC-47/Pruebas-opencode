package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(

        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String nombre,

        @Size(max = 300, message = "La biografia no puede superar los 300 caracteres")
        String bio,

        @Size(max = 500, message = "La URL del avatar no puede superar los 500 caracteres")
        String avatarUrl,

        @Pattern(
                regexp = "claro|oscuro|sistema",
                message = "El tema debe ser claro, oscuro o sistema")
        String tema,

        @Pattern(regexp = "es|en", message = "El idioma debe ser es o en")
        String idioma,

        Boolean notificacionesActivas
) {
}
