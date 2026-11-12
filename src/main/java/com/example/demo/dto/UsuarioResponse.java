package com.example.demo.dto;

import com.example.demo.entity.Usuario;

import java.util.List;

public record UsuarioResponse(Long id, String username, List<String> roles) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), List.of(usuario.getRol().name()));
    }
}
