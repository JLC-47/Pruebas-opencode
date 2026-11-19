package com.example.demo.dto;

import com.example.demo.entity.Usuario;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String username,
        List<String> roles,
        String nombre,
        String bio,
        String avatarUrl,
        String tema,
        String idioma,
        boolean notificacionesActivas) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                List.of(usuario.getRol().name()),
                usuario.getNombre(),
                usuario.getBio(),
                usuario.getAvatarUrl(),
                usuario.getTema(),
                usuario.getIdioma(),
                usuario.isNotificacionesActivas());
    }
}
