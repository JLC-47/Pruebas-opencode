package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.TokenInvalidoException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshTokenDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.jwt.refresh-token-days:7}") long refreshTokenDays) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new TokenInvalidoException("No fue posible iniciar sesion"));

        return generarRespuesta(usuario);
    }

    @Transactional
    public TokenResponse refrescar(String refreshToken) {
        RefreshToken tokenGuardado = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hashear(refreshToken))
                .orElseThrow(() -> new TokenInvalidoException("El refresh token es invalido o expiro"));

        if (tokenGuardado.getExpiresAt().isBefore(Instant.now())) {
            tokenGuardado.setRevoked(true);
            refreshTokenRepository.save(tokenGuardado);
            throw new TokenInvalidoException("El refresh token es invalido o expiro");
        }

        tokenGuardado.setRevoked(true);
        refreshTokenRepository.save(tokenGuardado);

        return generarRespuesta(tokenGuardado.getUsuario());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hashear(refreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private TokenResponse generarRespuesta(Usuario usuario) {
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshToken = generarRefreshToken();

        Instant ahora = Instant.now();
        RefreshToken tokenGuardado = new RefreshToken(
                null,
                usuario,
                hashear(refreshToken),
                ahora.plus(refreshTokenDays, ChronoUnit.DAYS),
                false,
                ahora);
        refreshTokenRepository.save(tokenGuardado);

        return new TokenResponse(
                accessToken,
                "Bearer",
                jwtService.accessTokenSeconds(),
                refreshToken,
                Duration.ofDays(refreshTokenDays).toSeconds(),
                UsuarioResponse.desde(usuario));
    }

    private String generarRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashear(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No fue posible generar el hash del refresh token", ex);
        }
    }
}
