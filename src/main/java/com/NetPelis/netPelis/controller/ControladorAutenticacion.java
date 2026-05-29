package com.NetPelis.netPelis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorAutenticacion {

    /**
     * Página de inicio: redirige al login o al dashboard según autenticación
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

    // ❌ ELIMINA ESTOS MÉTODOS - Ya existen en otros controllers:
    // - dashboardAdmin() → Ahora en AdminDashboardController
    // - dashboardCliente() → Debería estar en ClienteDashboardController
}