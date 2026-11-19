package com.example.demo.dto;

import com.example.demo.entity.Rol;
import jakarta.validation.constraints.NotNull;

public record RolRequest(@NotNull(message = "El rol es obligatorio") Rol rol) {
}
