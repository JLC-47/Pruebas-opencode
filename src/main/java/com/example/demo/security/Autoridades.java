package com.example.demo.security;

import com.example.demo.entity.Rol;
import org.springframework.security.core.Authentication;

public final class Autoridades {

    private Autoridades() {
    }

    public static boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
    }

    public static boolean esVendedor(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_VENDEDOR".equals(autoridad.getAuthority()));
    }

    public static Rol rol(Authentication authentication) {
        if (esAdmin(authentication)) {
            return Rol.ADMIN;
        }
        return esVendedor(authentication) ? Rol.VENDEDOR : Rol.USER;
    }
}
