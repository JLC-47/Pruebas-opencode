package com.example.demo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/login", "/registro", "/productos", "/productos/**", "/carrito", "/mis-compras"})
    public String redirigirAlFrontend() {
        return "forward:/index.html";
    }
}
