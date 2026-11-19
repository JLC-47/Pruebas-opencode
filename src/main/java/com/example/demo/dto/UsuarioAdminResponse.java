package com.example.demo.dto;

import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;

public record UsuarioAdminResponse(
        Long id,
        String username,
        Rol rol,
        boolean enabled,
        String nombre,
        String avatarUrl) {

    public static UsuarioAdminResponse desde(Usuario usuario) {
        return new UsuarioAdminResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.isEnabled(),
                usuario.getNombre(),
                usuario.getAvatarUrl());
    }
}
