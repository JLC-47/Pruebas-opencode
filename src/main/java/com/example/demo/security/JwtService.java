package com.example.demo.security;

import com.example.demo.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final String secret;
    private final long accessTokenMinutes;
    private SecretKey signingKey;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-minutes:20}") long accessTokenMinutes) {
        this.secret = secret;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    @PostConstruct
    void inicializarClave() {
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(Duration.ofMinutes(accessTokenMinutes));

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", List.of("ROLE_" + usuario.getRol().name()))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(signingKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long accessTokenSeconds() {
        return Duration.ofMinutes(accessTokenMinutes).toSeconds();
    }
}
