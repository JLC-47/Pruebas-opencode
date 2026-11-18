package com.example.demo.security;

import com.example.demo.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    static final String ISSUER_POR_DEFECTO = "productos-api";
    static final String AUDIENCE_POR_DEFECTO = "productos-web";
    static final long CLOCK_SKEW_SEGUNDOS_POR_DEFECTO = 30;
    private static final int MINIMO_BYTES_SECRETO = 32;

    private final String secret;
    private final long accessTokenMinutes;
    private final String issuer;
    private final String audience;
    private final long clockSkewSeconds;
    private SecretKey signingKey;

    @Autowired
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-minutes:20}") long accessTokenMinutes,
            @Value("${app.jwt.issuer:" + ISSUER_POR_DEFECTO + "}") String issuer,
            @Value("${app.jwt.audience:" + AUDIENCE_POR_DEFECTO + "}") String audience,
            @Value("${app.jwt.clock-skew-seconds:" + CLOCK_SKEW_SEGUNDOS_POR_DEFECTO + "}")
            long clockSkewSeconds) {
        this.secret = secret;
        this.accessTokenMinutes = accessTokenMinutes;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    JwtService(String secret, long accessTokenMinutes) {
        this(secret, accessTokenMinutes, ISSUER_POR_DEFECTO, AUDIENCE_POR_DEFECTO,
                CLOCK_SKEW_SEGUNDOS_POR_DEFECTO);
    }

    @PostConstruct
    void inicializarClave() {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("app.jwt.secret debe estar codificado en Base64", ex);
        }

        if (bytes.length < MINIMO_BYTES_SECRETO) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos " + MINIMO_BYTES_SECRETO
                            + " bytes (256 bits) codificados en Base64");
        }

        signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(Duration.ofMinutes(accessTokenMinutes));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .claim("roles", List.of("ROLE_" + usuario.getRol().name()))
                .claim("tipo", "access")
                .issuedAt(Date.from(ahora))
                .notBefore(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .clockSkewSeconds(clockSkewSeconds)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerUsername(String token) {
        return validarToken(token).getSubject();
    }

    public long accessTokenSeconds() {
        return Duration.ofMinutes(accessTokenMinutes).toSeconds();
    }
}
