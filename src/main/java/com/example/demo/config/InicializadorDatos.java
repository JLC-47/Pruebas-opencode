package com.example.demo.config;

import com.example.demo.entity.Producto;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class InicializadorDatos implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.seed.user.username:user}")
    private String userUsername;

    @Value("${app.seed.user.password:user123}")
    private String userPassword;

    @Override
    public void run(ApplicationArguments args) {
        crearUsuarioSiNoExiste(adminUsername, adminPassword, Rol.ADMIN);
        crearUsuarioSiNoExiste(userUsername, userPassword, Rol.USER);
        crearProductosSiNoExisten();
    }

    private void crearProductosSiNoExisten() {
        if (productoRepository.count() > 0) {
            return;
        }

        productoRepository.saveAll(List.of(
                new Producto(null, "Laptop Pro 14", new BigDecimal("4299000.00"), 12, "Tecnologia",
                        "Portatil ultraligero con pantalla 2.8K, 16 GB de RAM y 512 GB SSD."),
                new Producto(null, "Mouse Inalambrico Ergo", new BigDecimal("129900.00"), 38, "Tecnologia",
                        "Diseno ergonomico, conexion Bluetooth y bateria de larga duracion."),
                new Producto(null, "Teclado Mecanico Compacto", new BigDecimal("289900.00"), 24, "Tecnologia",
                        "Teclas mecanicas silenciosas, retroiluminacion y conexion USB-C."),
                new Producto(null, "Monitor 27 Pulgadas 4K", new BigDecimal("1899000.00"), 9, "Tecnologia",
                        "Panel IPS 4K con HDR, ideal para diseno y productividad."),
                new Producto(null, "Audifonos Bluetooth Pro", new BigDecimal("399900.00"), 31, "Tecnologia",
                        "Cancelacion de ruido, sonido envolvente y hasta 30 horas de bateria."),
                new Producto(null, "Camara Web Full HD", new BigDecimal("219900.00"), 18, "Tecnologia",
                        "Video 1080p, microfono integrado y enfoque automatico para videollamadas."),
                new Producto(null, "Parlante Portatil", new BigDecimal("249900.00"), 22, "Tecnologia",
                        "Sonido potente, resistencia al agua y autonomia de 12 horas."),
                new Producto(null, "Smartwatch Active", new BigDecimal("549900.00"), 15, "Tecnologia",
                        "Monitoreo de actividad, notificaciones y pantalla AMOLED."),
                new Producto(null, "Cafetera Express", new BigDecimal("899900.00"), 8, "Hogar",
                        "Prepara espresso y capuchino con presion de 20 bares."),
                new Producto(null, "Licuadora de Alta Potencia", new BigDecimal("459900.00"), 14, "Hogar",
                        "Motor de 1200 W, jarra de vidrio y multiples velocidades."),
                new Producto(null, "Freidora de Aire 6L", new BigDecimal("649900.00"), 11, "Hogar",
                        "Cocina con poco aceite, 8 programas y canasta antiadherente."),
                new Producto(null, "Lampara de Escritorio LED", new BigDecimal("119900.00"), 27, "Hogar",
                        "Luz regulable, brazo articulado y puerto de carga USB."),
                new Producto(null, "Silla Ergonomica Ejecutiva", new BigDecimal("1199000.00"), 7, "Oficina",
                        "Soporte lumbar, reposabrazos ajustables y malla transpirable."),
                new Producto(null, "Escritorio Elevable", new BigDecimal("1599000.00"), 5, "Oficina",
                        "Altura ajustable electricamente, estructura reforzada y memoria de posiciones."),
                new Producto(null, "Mochila para Laptop", new BigDecimal("189900.00"), 33, "Accesorios",
                        "Compartimento acolchado para 15 pulgadas y puerto USB externo."),
                new Producto(null, "Hub USB-C 7 en 1", new BigDecimal("159900.00"), 41, "Accesorios",
                        "HDMI 4K, lector SD, USB 3.0 y carga rapida en un solo dispositivo."),
                new Producto(null, "Base para Laptop", new BigDecimal("79900.00"), 46, "Accesorios",
                        "Soporte ajustable de aluminio que mejora la postura y la ventilacion."),
                new Producto(null, "Organizador de Escritorio", new BigDecimal("69900.00"), 52, "Accesorios",
                        "Bandejas modulares para mantener cables, notas y accesorios en orden.")
        ));
        log.info("Catalogo inicial de productos creado");
    }

    private void crearUsuarioSiNoExiste(String username, String password, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }

        usuarioRepository.save(new Usuario(
                null,
                username,
                passwordEncoder.encode(password),
                rol,
                true));
        log.info("Usuario {} con rol {} inicializado", username, rol);
    }
}
