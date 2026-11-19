package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 80)
    private String nombre;

    @Column(length = 300)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 20)
    private String tema = "claro";

    @Column(length = 10)
    private String idioma = "es";

    @Column(name = "notificaciones_activas", nullable = false)
    private boolean notificacionesActivas = true;

    public Usuario(Long id, String username, String password, Rol rol, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.enabled = enabled;
        this.tema = "claro";
        this.idioma = "es";
        this.notificacionesActivas = true;
    }
}
