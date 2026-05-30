package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.*;
import com.NetPelis.netPelis.service.impl.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Controller para navegación del panel de administración
 * RUTAS: /admin/* (EXCEPTO /admin/peliculas que es de PeliculaController)
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioResena repositorioResena;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioGenero repositorioGenero;

    // ✅ Inyectar servicio de usuarios para búsqueda con filtros
    private final UsuarioService usuarioService;

    /**
     * Dashboard principal de admin
     * RUTA: GET /admin/dashboard
     * TEMPLATE: templates/admin-dashboard.html
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalResenas", contarResenasEsteMes());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());
            model.addAttribute("peliculasRecientes", repositorioPelicula.findRecientesConGeneros());
            model.addAttribute("resenasRecientes", repositorioResena.findRecientesConUsuario(5));
            model.addAttribute("todosLosGeneros", repositorioGenero.findAll());

            System.out.println("✅ Dashboard cargado:");
            System.out.println("   - Películas: " + repositorioPelicula.count());
            System.out.println("   - Usuarios: " + repositorioUsuario.countByActivoTrue());
            System.out.println("   - Reseñas este mes: " + contarResenasEsteMes());

        } catch (Exception e) {
            System.err.println("❌ Error cargando dashboard: " + e.getMessage());
            e.printStackTrace();
        }

        return "admin-dashboard";  // ✅ Busca: templates/admin-dashboard.html
    }

    /**
     * ✅ Página de gestión de usuarios CON BÚSQUEDA Y FILTROS
     * RUTA: GET /admin/usuarios
     * TEMPLATE: templates/admin/usuarios.html
     *
     * Parámetros de filtro (opcionales):
     * - id: Long
     * - nombre: String (búsqueda parcial, case-insensitive)
     * - email: String (búsqueda parcial, case-insensitive)
     * - rol: RolUsuario (ADMIN o CLIENTE)
     */


    // ❌ NO agregar @GetMapping("/peliculas") aquí - Eso es de PeliculaController

    /**
     * Utilidad: Contar reseñas del mes actual
     */
    private long contarResenasEsteMes() {
        try {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime inicio = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime fin = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            return repositorioResena.countByFechaCreacionBetween(inicio, fin);
        } catch (Exception e) {
            return 0;
        }
    }
}