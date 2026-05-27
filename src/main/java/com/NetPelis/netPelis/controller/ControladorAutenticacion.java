package com.NetPelis.netPelis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class ControladorAutenticacion {

    @GetMapping("/")
    public String paginaInicio() {
        return "login";
    }

    @GetMapping("/redireccionar-segun-rol")
    public String redireccionarSegunRol(Authentication auth) {
        if (auth == null) return "redirect:/?error=true";

        String rol = auth.getAuthorities().iterator().next().getAuthority();
        return rol.contains("ADMIN") ? "redirect:/admin-dashboard" : "redirect:/cliente-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String dashboardAdmin() {
        return "admin-dashboard";
    }

    @GetMapping("/cliente-dashboard")
    public String dashboardCliente() {
        return "cliente-dashboard";
    }
}