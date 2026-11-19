package com.example.demo.service;

import com.example.demo.dto.CambioContrasenaRequest;
import com.example.demo.dto.PerfilRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private static final String TEMA_POR_DEFECTO = "claro";
    private static final String IDIOMA_POR_DEFECTO = "es";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PerfilResponse obtener(String username) {
        return PerfilResponse.desde(buscarUsuario(username));
    }

    @Transactional
    public PerfilResponse actualizar(String username, PerfilRequest request) {
        Usuario usuario = buscarUsuario(username);

        usuario.setNombre(normalizar(request.nombre()));
        usuario.setBio(normalizar(request.bio()));
        usuario.setAvatarUrl(normalizar(request.avatarUrl()));
        usuario.setTema(request.tema() == null || request.tema().isBlank()
                ? TEMA_POR_DEFECTO
                : request.tema());
        usuario.setIdioma(request.idioma() == null || request.idioma().isBlank()
                ? IDIOMA_POR_DEFECTO
                : request.idioma());
        if (request.notificacionesActivas() != null) {
            usuario.setNotificacionesActivas(request.notificacionesActivas());
        }

        return PerfilResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarContrasena(String username, CambioContrasenaRequest request) {
        Usuario usuario = buscarUsuario(username);

        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getPassword())) {
            throw new ParametrosInvalidosException("La contrasena actual no es correcta");
        }
        if (passwordEncoder.matches(request.contrasenaNueva(), usuario.getPassword())) {
            throw new ParametrosInvalidosException("La nueva contrasena debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.contrasenaNueva()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNotFoundException(username));
    }

    private String normalizar(String valor) {
        String recortado = valor == null ? null : valor.trim();
        return recortado == null || recortado.isEmpty() ? null : recortado;
    }
}
