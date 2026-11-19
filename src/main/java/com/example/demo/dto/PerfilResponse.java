package com.example.demo.dto;

import com.example.demo.entity.Usuario;

import java.util.List;

public record PerfilResponse(
        Long id,
        String username,
        List<String> roles,
        String nombre,
        String bio,
        String avatarUrl,
        String tema,
        String idioma,
        boolean notificacionesActivas,
        boolean enabled) {

    public static PerfilResponse desde(Usuario usuario) {
        return new PerfilResponse(
                usuario.getId(),
                usuario.getUsername(),
                List.of(usuario.getRol().name()),
                usuario.getNombre(),
                usuario.getBio(),
                usuario.getAvatarUrl(),
                usuario.getTema(),
                usuario.getIdioma(),
                usuario.isNotificacionesActivas(),
                usuario.isEnabled());
    }
}
