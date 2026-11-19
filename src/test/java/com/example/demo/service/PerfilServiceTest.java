package com.example.demo.service;

import com.example.demo.dto.CambioContrasenaRequest;
import com.example.demo.dto.PerfilRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PerfilService service;

    @Test
    void obtenerDevuelveElPerfilDelUsuario() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario()));

        PerfilResponse perfil = service.obtener("user");

        assertThat(perfil.username()).isEqualTo("user");
        assertThat(perfil.roles()).containsExactly("USER");
        assertThat(perfil.tema()).isEqualTo("claro");
        assertThat(perfil.notificacionesActivas()).isTrue();
    }

    @Test
    void obtenerLanzaExcepcionCuandoNoExisteElUsuario() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener("nadie"))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("nadie");
    }

    @Test
    void actualizarPersonalizaElPerfil() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        PerfilResponse perfil = service.actualizar("user", new PerfilRequest(
                "  Ana Perez  ",
                "  Me gusta la tecnologia  ",
                "  /avatar.png  ",
                "oscuro",
                "en",
                false));

        assertThat(perfil.nombre()).isEqualTo("Ana Perez");
        assertThat(perfil.bio()).isEqualTo("Me gusta la tecnologia");
        assertThat(perfil.avatarUrl()).isEqualTo("/avatar.png");
        assertThat(perfil.tema()).isEqualTo("oscuro");
        assertThat(perfil.idioma()).isEqualTo("en");
        assertThat(perfil.notificacionesActivas()).isFalse();
        assertThat(usuario.getUsername()).isEqualTo("user");
    }

    @Test
    void actualizarAplicaValoresPorDefectoCuandoFaltanPreferencias() {
        Usuario usuario = usuario();
        usuario.setTema("oscuro");
        usuario.setIdioma("en");
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        PerfilResponse perfil = service.actualizar("user", new PerfilRequest(
                "", "   ", null, null, null, null));

        assertThat(perfil.nombre()).isNull();
        assertThat(perfil.bio()).isNull();
        assertThat(perfil.tema()).isEqualTo("claro");
        assertThat(perfil.idioma()).isEqualTo("es");
        assertThat(perfil.notificacionesActivas()).isTrue();
    }

    @Test
    void cambiarContrasenaValidaLaActualYGuardaElHash() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveActual123", usuario.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("claveNueva123", usuario.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("claveNueva123")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        service.cambiarContrasena("user", new CambioContrasenaRequest("claveActual123", "claveNueva123"));

        assertThat(usuario.getPassword()).isEqualTo("hash-nuevo");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarContrasenaRechazaLaContrasenaActualIncorrecta() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", usuario.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.cambiarContrasena(
                "user", new CambioContrasenaRequest("incorrecta", "claveNueva123")))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("contrasena actual");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenaRechazaRepetirLaContrasena() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveActual123", usuario.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> service.cambiarContrasena(
                "user", new CambioContrasenaRequest("claveActual123", "claveActual123")))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("diferente");
    }

    private Usuario usuario() {
        return new Usuario(1L, "user", "hash-actual", Rol.USER, true);
    }
}
