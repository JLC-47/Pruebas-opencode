package com.example.demo.dto;

import java.util.List;

public record RolResponse(String nombre, String descripcion, List<String> permisos) {
}
