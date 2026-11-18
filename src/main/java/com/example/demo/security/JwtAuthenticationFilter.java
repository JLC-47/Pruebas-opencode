package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request.getHeader("Authorization"));
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extraerUsername(token);
            UserDetails usuario = usuarioDetailsService.loadUserByUsername(username);

            if (!usuario.isEnabled()) {
                SecurityContextHolder.clearContext();
                log.debug("Token rechazado para el usuario deshabilitado {}", username);
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            log.debug("Token JWT rechazado: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(String encabezado) {
        if (encabezado == null
                || !encabezado.regionMatches(true, 0, PREFIJO_BEARER, 0, PREFIJO_BEARER.length())) {
            return null;
        }

        String token = encabezado.substring(PREFIJO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
