package com.example.demo.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        UsuarioResponse usuario
) {
}
