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

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }


    @PostMapping("/logout")
    public String logout() {
        return "redirect:/auth/login?logout=true";
    }
}