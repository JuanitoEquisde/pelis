package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.dto.UsuarioResenaDTO;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
public class AdminReportesController {

    private final RepositorioResena repositorioResena;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioFavorito repositorioFavorito;

    /**
     * Dashboard de reportes
     */
    @GetMapping
    public String dashboardReportes(Model model) {
        model.addAttribute("totalPeliculas", repositorioPelicula.count());
        model.addAttribute("totalUsuarios", repositorioUsuario.count());
        model.addAttribute("totalResenas", repositorioResena.count());
        model.addAttribute("totalFavoritos", repositorioFavorito.count());

        return "admin/reportes-dashboard";
    }
    /**
     * Reporte: Usuarios con más reseñas
     * ✅ CORREGIDO: Aplicar límite en Java en lugar de JPQL
     */
    @GetMapping("/usuarios-activos")
    public String usuariosConMasResenas(
            @RequestParam(defaultValue = "10") int limit,
            Model model) {

        // ✅ Obtener todos los usuarios con reseñas y aplicar límite en Java
        // (Hibernate 6 no soporta LIMIT con constructor expressions en JPQL)
        List<UsuarioResenaDTO> todos = repositorioResena.findUsuariosConMasResenas();
        List<UsuarioResenaDTO> reporte = todos.stream()
                .limit(limit)
                .toList();

        model.addAttribute("reporteUsuarios", reporte);
        model.addAttribute("totalRegistros", reporte.size());
        model.addAttribute("limit", limit);

        // Stats adicionales
        model.addAttribute("totalPeliculas", repositorioPelicula.count());
        model.addAttribute("totalUsuarios", repositorioUsuario.count());
        model.addAttribute("totalResenas", repositorioResena.count());
        model.addAttribute("totalFavoritos", repositorioFavorito.count());

        return "admin/usuarios-activos";
    }
}