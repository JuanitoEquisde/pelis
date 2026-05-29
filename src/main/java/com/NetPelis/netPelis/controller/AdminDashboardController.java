package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.EstadoPelicula;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioGenero;  // ✅ IMPORTAR
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioResena repositorioResena;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioGenero repositorioGenero;  // ✅ AGREGAR

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Estadísticas generales
            long totalPeliculas = repositorioPelicula.count();
            long totalUsuarios = repositorioUsuario.countByActivoTrue();
            long totalResenas = contarResenasEsteMes();
            long totalFavoritos = repositorioFavorito.count();

            model.addAttribute("totalPeliculas", totalPeliculas);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalResenas", totalResenas);
            model.addAttribute("totalFavoritos", totalFavoritos);

            // Películas recientes para la tabla (últimas 10)
            model.addAttribute("peliculasRecientes",
                    repositorioPelicula.findRecientesConGeneros());

            // Reseñas recientes para actividad (últimas 5)
            model.addAttribute("resenasRecientes",
                    repositorioResena.findRecientesConUsuario(5));

            // ✅ AGREGAR: Lista de todos los géneros para el modal
            model.addAttribute("todosLosGeneros", repositorioGenero.findAll());

            System.out.println("✅ Dashboard cargado:");
            System.out.println("   - Películas: " + totalPeliculas);
            System.out.println("   - Usuarios: " + totalUsuarios);
            System.out.println("   - Reseñas este mes: " + totalResenas);
            System.out.println("   - Favoritos: " + totalFavoritos);
            System.out.println("   - Géneros cargados: " + repositorioGenero.count());

        } catch (Exception e) {
            System.err.println("❌ Error cargando dashboard: " + e.getMessage());
            e.printStackTrace();
        }

        return "admin-dashboard";
    }

    private long contarResenasEsteMes() {
        try {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime inicioMes = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime finMes = currentMonth.atEndOfMonth().atTime(23, 59, 59);

            return repositorioResena.countByFechaCreacionBetween(inicioMes, finMes);
        } catch (Exception e) {
            return 0;
        }
    }
}