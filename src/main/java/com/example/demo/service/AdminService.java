package com.example.demo.service;

import com.example.demo.dto.RolRequest;
import com.example.demo.dto.RolResponse;
import com.example.demo.dto.UsuarioAdminRequest;
import com.example.demo.dto.UsuarioAdminResponse;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.exception.UsuarioYaExisteException;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioAdminResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioAdminResponse obtener(Long id) {
        return UsuarioAdminResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioAdminResponse crear(UsuarioAdminRequest request) {
        String username = request.username().trim();
        validarUsernameLibre(username, null);
        validarContrasena(request.password(), true);

        Usuario usuario = new Usuario(
                null,
                username,
                passwordEncoder.encode(request.password()),
                request.rol(),
                request.enabled() == null || request.enabled());

        return UsuarioAdminResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse actualizar(Long id, UsuarioAdminRequest request, String adminUsername) {
        Usuario usuario = buscar(id);
        String username = request.username().trim();
        validarUsernameLibre(username, id);
        validarContrasena(request.password(), false);
        validarUltimoAdmin(usuario, request.rol(), request.enabled() == null || request.enabled());

        usuario.setUsername(username);
        usuario.setRol(request.rol());
        usuario.setEnabled(request.enabled() == null || request.enabled());
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return UsuarioAdminResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse cambiarRol(Long id, RolRequest request, String adminUsername) {
        Usuario usuario = buscar(id);
        validarNoPropio(id, adminUsername, "cambiar tu propio rol");
        validarUltimoAdmin(usuario, request.rol(), usuario.isEnabled());

        usuario.setRol(request.rol());
        return UsuarioAdminResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse cambiarEstado(Long id, boolean enabled, String adminUsername) {
        Usuario usuario = buscar(id);
        validarNoPropio(id, adminUsername, (enabled ? "activar" : "desactivar") + " tu propia cuenta");
        validarUltimoAdmin(usuario, usuario.getRol(), enabled);

        usuario.setEnabled(enabled);
        return UsuarioAdminResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String adminUsername) {
        Usuario usuario = buscar(id);
        validarNoPropio(id, adminUsername, "eliminar tu propia cuenta");
        if (usuario.getRol() == Rol.ADMIN && contarAdmins() <= 1) {
            throw new ParametrosInvalidosException("No puedes eliminar al ultimo administrador");
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public List<RolResponse> roles() {
        return List.of(
                new RolResponse(
                        Rol.ADMIN.name(),
                        "Control total del catalogo, los usuarios y los roles",
                        List.of(
                                "Consultar y gestionar productos",
                                "Consultar, crear, editar, eliminar y asignar roles a usuarios",
                                "Consultar el panel de ventas",
                                "Comprar y devolver pedidos")),
                new RolResponse(
                        Rol.VENDEDOR.name(),
                        "Gestiona su propio catalogo y consulta sus ventas",
                        List.of(
                                "Crear, editar y eliminar solo sus productos",
                                "Consultar sus productos y sus ventas",
                                "Comprar y devolver pedidos")),
                new RolResponse(
                        Rol.USER.name(),
                        "Cliente del catalogo",
                        List.of("Consultar productos", "Comprar y devolver pedidos", "Personalizar su perfil")));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    private void validarUsernameLibre(String username, Long idIgnorado) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            if (idIgnorado == null || !usuario.getId().equals(idIgnorado)) {
                throw new UsuarioYaExisteException("El usuario '" + username + "' ya esta registrado");
            }
        });
    }

    private void validarContrasena(String password, boolean obligatoria) {
        if (obligatoria && (password == null || password.isBlank())) {
            throw new ParametrosInvalidosException("La contrasena es obligatoria");
        }
        if (password != null && !password.isBlank()
                && (password.length() < LONGITUD_MINIMA_CONTRASENA
                || password.length() > LONGITUD_MAXIMA_CONTRASENA)) {
            throw new ParametrosInvalidosException(
                    "La contrasena debe tener entre " + LONGITUD_MINIMA_CONTRASENA
                            + " y " + LONGITUD_MAXIMA_CONTRASENA + " caracteres");
        }
    }

    private void validarNoPropio(Long id, String adminUsername, String accion) {
        Usuario usuario = buscar(id);
        if (usuario.getUsername().equals(adminUsername)) {
            throw new ParametrosInvalidosException("No puedes " + accion);
        }
    }

    private void validarUltimoAdmin(Usuario usuario, Rol nuevoRol, boolean habilitado) {
        if (usuario.getRol() == Rol.ADMIN
                && (nuevoRol != Rol.ADMIN || !habilitado)
                && contarAdmins() <= 1) {
            throw new ParametrosInvalidosException("No puedes modificar el rol del ultimo administrador");
        }
    }

    private long contarAdmins() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRol() == Rol.ADMIN && usuario.isEnabled())
                .count();
    }
}
