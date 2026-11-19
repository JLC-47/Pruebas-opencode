package com.example.demo.service;

import com.example.demo.dto.RolRequest;
import com.example.demo.dto.RolResponse;
import com.example.demo.dto.UsuarioAdminRequest;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ParametrosInvalidosException;
import com.example.demo.exception.UsuarioNotFoundException;
import com.example.demo.exception.UsuarioYaExisteException;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService service;

    @Test
    void listarDevuelveTodosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(admin(), usuario(2L, "user", Rol.USER)));

        assertThat(service.listar()).extracting("username").containsExactly("admin", "user");
    }

    @Test
    void obtenerDevuelveElUsuarioSolicitado() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "user", Rol.USER)));

        assertThat(service.obtener(2L).rol()).isEqualTo(Rol.USER);
    }

    @Test
    void obtenerLanzaExcepcionCuandoNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crearAsignaElRolIndicadoYHashLaContrasena() {
        when(usuarioRepository.findByUsername("vendedor2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave12345")).thenReturn("hash");
        when(usuarioRepository.save(org.mockito.ArgumentMatchers.any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario guardado = invocacion.getArgument(0);
                    guardado.setId(5L);
                    return guardado;
                });

        var creado = service.crear(new UsuarioAdminRequest(
                "vendedor2", "clave12345", Rol.VENDEDOR, true));

        assertThat(creado.id()).isEqualTo(5L);
        assertThat(creado.rol()).isEqualTo(Rol.VENDEDOR);
        assertThat(creado.enabled()).isTrue();
    }

    @Test
    void crearRechazaUnUsernameDuplicado() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> service.crear(new UsuarioAdminRequest(
                "admin", "clave12345", Rol.USER, true)))
                .isInstanceOf(UsuarioYaExisteException.class);
    }

    @Test
    void crearRechazaUnaContrasenaCorta() {
        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new UsuarioAdminRequest(
                "nuevo", "corta", Rol.USER, true)))
                .isInstanceOf(ParametrosInvalidosException.class);
    }

    @Test
    void crearExigeContrasena() {
        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new UsuarioAdminRequest(
                "nuevo", "   ", Rol.USER, true)))
                .isInstanceOf(ParametrosInvalidosException.class);
    }

    @Test
    void actualizarCambiaRolEstadoYContrasena() {
        Usuario objetivo = usuario(2L, "cliente", Rol.USER);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.findByUsername("cliente2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(objetivo)).thenReturn(objetivo);

        var actualizado = service.actualizar(2L, new UsuarioAdminRequest(
                "cliente2", "nuevaClave123", Rol.VENDEDOR, false), "admin");

        assertThat(actualizado.username()).isEqualTo("cliente2");
        assertThat(actualizado.rol()).isEqualTo(Rol.VENDEDOR);
        assertThat(actualizado.enabled()).isFalse();
        assertThat(objetivo.getPassword()).isEqualTo("hash-nuevo");
    }

    @Test
    void actualizarConservaLaContrasenaCuandoNoSeEnvia() {
        Usuario objetivo = usuario(2L, "cliente", Rol.USER);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.save(objetivo)).thenReturn(objetivo);

        service.actualizar(2L, new UsuarioAdminRequest("cliente", null, Rol.USER, true), "admin");

        assertThat(objetivo.getPassword()).isEqualTo("hash");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void cambiarRolAsignaElRolSolicitado() {
        Usuario objetivo = usuario(2L, "cliente", Rol.USER);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.save(objetivo)).thenReturn(objetivo);

        assertThat(service.cambiarRol(2L, new RolRequest(Rol.VENDEDOR), "admin").rol())
                .isEqualTo(Rol.VENDEDOR);
    }

    @Test
    void cambiarRolNoPermiteModificarElPropioRol() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> service.cambiarRol(1L, new RolRequest(Rol.USER), "admin"))
                .isInstanceOf(ParametrosInvalidosException.class);
    }

    @Test
    void cambiarRolNoPermiteDesactivarAlUltimoAdmin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(usuarioRepository.findAll()).thenReturn(List.of(admin()));

        assertThatThrownBy(() -> service.cambiarEstado(1L, false, "otro-admin"))
                .isInstanceOf(ParametrosInvalidosException.class)
                .hasMessageContaining("ultimo administrador");
    }

    @Test
    void cambiarEstadoActivaUnUsuario() {
        Usuario objetivo = usuario(2L, "cliente", Rol.USER);
        objetivo.setEnabled(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.save(objetivo)).thenReturn(objetivo);

        assertThat(service.cambiarEstado(2L, true, "admin").enabled()).isTrue();
    }

    @Test
    void eliminarBorraAlUsuario() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "cliente", Rol.USER)));

        service.eliminar(2L, "admin");

        verify(usuarioRepository).delete(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    @Test
    void eliminarNoPermiteBorrarAlUltimoAdmin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(usuarioRepository.findAll()).thenReturn(List.of(admin()));

        assertThatThrownBy(() -> service.eliminar(1L, "otro-admin"))
                .isInstanceOf(ParametrosInvalidosException.class);
    }

    @Test
    void rolesIncluyeLosTresPerfilesConSusPermisos() {
        List<RolResponse> roles = service.roles();

        assertThat(roles).extracting("nombre").containsExactly("ADMIN", "VENDEDOR", "USER");
        assertThat(roles.get(1).permisos()).isNotEmpty();
    }

    private Usuario admin() {
        return new Usuario(1L, "admin", "hash", Rol.ADMIN, true);
    }

    private Usuario usuario(Long id, String username, Rol rol) {
        return new Usuario(id, username, "hash", rol, true);
    }
}
