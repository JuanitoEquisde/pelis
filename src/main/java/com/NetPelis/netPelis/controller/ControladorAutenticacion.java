package com.NetPelis.netPelis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para redirecciones inteligentes post-login
 * RUTAS: / y /redireccionar-segun-rol
 */
@Controller
public class ControladorAutenticacion {

    /**
     * Página de inicio: redirige según estado de autenticación
     * RUTA: GET /
     */
    @GetMapping("/")
    public String paginaInicio() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().toString().equals("anonymousUser")) {
            return "redirect:/redireccionar-segun-rol";
        }
        return "redirect:/auth/login";
    }

    /**
     * Redirección inteligente según el rol del usuario
     * RUTA: GET /redireccionar-segun-rol
     */
    @GetMapping("/redireccionar-segun-rol")
    public String redireccionarSegunRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return "redirect:/auth/login?error=sesion-expirada";
        }

        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        if (rol.contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (rol.contains("ROLE_CLIENTE")) {
            return "redirect:/cliente/dashboard";
        }

        return "redirect:/auth/login?error=rol-invalido";
    }
}