package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
        assertThat(claims.getIssuer()).isEqualTo(JwtService.ISSUER_POR_DEFECTO);
        assertThat(claims.getAudience()).containsExactly(JwtService.AUDIENCE_POR_DEFECTO);
        assertThat(claims.get("tipo")).isEqualTo("access");
        assertThat(((Number) claims.get("uid")).longValue()).isEqualTo(1L);
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(Date.from(Instant.now()));
        assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now().plusSeconds(1100)));
    }

    @Test
    void generaUnIdentificadorUnicoParaCadaToken() {
        Usuario usuario = new Usuario(1L, "admin", "hash", Rol.ADMIN, true);

        String primerToken = jwtService.generarAccessToken(usuario);
        String segundoToken = jwtService.generarAccessToken(usuario);

        assertThat(jwtService.validarToken(primerToken).getId())
                .isNotEqualTo(jwtService.validarToken(segundoToken).getId());
    }

    @Test
    void rechazaUnTokenConOtroIssuer() {
        String token = Jwts.builder()
                .issuer("otro-emisor")
                .audience().add(JwtService.AUDIENCE_POR_DEFECTO).and()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validarToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenConOtraAudiencia() {
        String token = Jwts.builder()
                .issuer(JwtService.ISSUER_POR_DEFECTO)
                .audience().add("otra-aplicacion").and()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validarToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenExpiradoFueraDelMargenDeTolerancia() {
        String token = Jwts.builder()
                .issuer(JwtService.ISSUER_POR_DEFECTO)
                .audience().add(JwtService.AUDIENCE_POR_DEFECTO).and()
                .subject("admin")
                .issuedAt(Date.from(Instant.now().minusSeconds(600)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validarToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rechazaUnSecretoDemasiadoCorto() {
        String secretoCorto = Base64.getEncoder()
                .encodeToString("clave-corta".getBytes(StandardCharsets.UTF_8));
        JwtService servicioConSecretoCorto = new JwtService(secretoCorto, 20);

        assertThatThrownBy(servicioConSecretoCorto::inicializarClave)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }

    @Test
    void rechazaUnSecretoQueNoEstaEnBase64() {
        JwtService servicioConSecretoInvalido = new JwtService("%%%no-es-base64%%%", 20);

        assertThatThrownBy(servicioConSecretoInvalido::inicializarClave)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
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
