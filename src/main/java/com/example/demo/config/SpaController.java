package com.example.demo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/productos", "/productos/**", "/login"})
    public String redirigirAlFrontend() {
        return "forward:/index.html";
    }
}
