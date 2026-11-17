package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("clave-de-prueba-con-al-menos-32-bytes-para-hs256"
                    .getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void configurar() {
        jwtService = new JwtService(SECRET, 20);
        jwtService.inicializarClave();
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    @Test
    void generaUnTokenConElUsuarioSuRolYFechaDeExpiracion() {
        Usuario usuario = new Usuario(1L, "admin", "hash", Rol.ADMIN, true);

        String token = jwtService.generarAccessToken(usuario);
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("roles")).isEqualTo(List.of("ROLE_ADMIN"));
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(Date.from(Instant.now()));
        assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now().plusSeconds(1100)));
    }

    @Test
    void extraeElUsernameDelToken() {
        Usuario usuario = new Usuario(2L, "cliente", "hash", Rol.USER, true);

        assertThat(jwtService.extraerUsername(jwtService.generarAccessToken(usuario)))
                .isEqualTo("cliente");
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = jwtService.generarAccessToken(new Usuario(3L, "admin", "hash", Rol.ADMIN, true));
        String otraClave = Base64.getEncoder()
                .encodeToString("otra-clave-de-prueba-con-al-menos-48-bytes-para-hs384"
                        .getBytes(StandardCharsets.UTF_8));
        SecretKey otraClaveFirmante = Keys.hmacShaKeyFor(Decoders.BASE64.decode(otraClave));

        assertThatThrownBy(() -> Jwts.parser()
                        .verifyWith(otraClaveFirmante)
                        .build()
                        .parseSignedClaims(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void exponeLaDuracionDelAccessTokenEnSegundos() {
        assertThat(jwtService.accessTokenSeconds()).isEqualTo(1200);
    }
}
