package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.EstadoPelicula;
import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteDashboardController {

    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioResena repositorioResena;

    /**
     * Dashboard principal del cliente
     * Muestra películas disponibles, favoritos y actividad del usuario
     */
    @GetMapping("/dashboard")
    public String dashboardCliente(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            // ✅ Query simple y directa
            List<com.NetPelis.netPelis.entity.Pelicula> peliculas =
                    repositorioPelicula.findByEstado(EstadoPelicula.PUBLICADO);

            // ✅ Logs obligatorios para debug
            System.out.println("🎬 DEBUG - Usuario: " + usuario.getNombreCompleto());
            System.out.println("🎬 DEBUG - Películas PUBLICADO: " + peliculas.size());
            for (var p : peliculas) {
                System.out.println("   - [" + p.getId() + "] " + p.getTitulo() + " | Estado: " + p.getEstado());
            }

            // ✅ NOMBRE EXACTO: "peliculas" (case-sensitive)
            model.addAttribute("peliculas", peliculas);
            model.addAttribute("usuario", usuario);
            model.addAttribute("totalFavoritos", repositorioFavorito.countByUsuarioId(usuario.getId()));
            model.addAttribute("totalResenasUsuario", repositorioResena.countByUsuarioId(usuario.getId()));

            return "cliente-dashboard";

        } catch (Exception e) {
            System.err.println("❌ ERROR en dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("peliculas", List.of());
            return "cliente-dashboard";
        }
    }
    /**
     * Página de películas disponibles para ver
     */
    @GetMapping("/peliculas")
    public String verPeliculas(Model model) {
        try {
            List<com.NetPelis.netPelis.entity.Pelicula> peliculas =
                    repositorioPelicula.findByEstado(EstadoPelicula.PUBLICADO);

            model.addAttribute("peliculas", peliculas);
            model.addAttribute("titulo", "Catálogo de Películas");

            System.out.println("✅ Página de películas cargada: " + peliculas.size() + " películas");
            return "cliente/peliculas";
        } catch (Exception e) {
            System.err.println("Error cargando películas: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cliente/dashboard";
        }
    }

    /**
     * Página de mis favoritos
     */
    @GetMapping("/favoritos")
    public String misFavoritos(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<com.NetPelis.netPelis.entity.Favorito> favoritos =
                    repositorioFavorito.findByUsuarioId(usuario.getId());

            model.addAttribute("favoritos", favoritos);
            model.addAttribute("titulo", "Mis Películas Favoritas");

            return "cliente/favoritos";
        } catch (Exception e) {
            System.err.println("Error cargando favoritos: " + e.getMessage());
            return "redirect:/cliente/dashboard";
        }
    }

    /**
     * Página de mis reseñas
     */
    @GetMapping("/mis-resenas")
    public String misResenas(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<com.NetPelis.netPelis.entity.Resena> resenas =
                    repositorioResena.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());

            model.addAttribute("resenas", resenas);
            model.addAttribute("titulo", "Mis Reseñas");

            return "cliente/mis-resenas";
        } catch (Exception e) {
            System.err.println("Error cargando reseñas: " + e.getMessage());
            return "redirect:/cliente/dashboard";
        }
    }
}