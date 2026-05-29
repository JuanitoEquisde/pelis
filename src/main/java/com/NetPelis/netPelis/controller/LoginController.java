package com.NetPelis.netPelis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller para autenticación: login y logout
 * RUTAS: /auth/*
 */
@Controller
@RequestMapping("/auth")
public class LoginController {

    /**
     * Muestra la página de login
     * RUTA: GET /auth/login
     * TEMPLATE: templates/login.html
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    /**
     * Procesa el logout (Spring Security maneja la lógica)
     * RUTA: POST /auth/logout
     */
    @PostMapping("/logout")
    public String logout() {
        return "redirect:/auth/login?logout=true";
    }
}