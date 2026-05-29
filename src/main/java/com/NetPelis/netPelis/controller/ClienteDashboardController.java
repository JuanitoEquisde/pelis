package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.EstadoPelicula;
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
            // Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            // Buscar usuario por email
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            // Estadísticas del cliente
            long totalFavoritos = repositorioFavorito.countByUsuarioId(usuario.getId());
            long totalResenasUsuario = repositorioResena.countByUsuarioId(usuario.getId());
            long totalPeliculasDisponibles = repositorioPelicula.countByEstado(EstadoPelicula.PUBLICADO);

            // Películas publicadas disponibles
            List<com.NetPelis.netPelis.entity.Pelicula> peliculasDisponibles =
                    repositorioPelicula.findByEstado(EstadoPelicula.PUBLICADO);

            // Películas favoritas del usuario
            List<com.NetPelis.netPelis.entity.Favorito> misFavoritos =
                    repositorioFavorito.findByUsuarioId(usuario.getId());

            // Últimas reseñas del usuario
            List<com.NetPelis.netPelis.entity.Resena> misResenas =
                    repositorioResena.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());

            // Agregar atributos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("totalFavoritos", totalFavoritos);
            model.addAttribute("totalResenasUsuario", totalResenasUsuario);
            model.addAttribute("totalPeliculasDisponibles", totalPeliculasDisponibles);
            model.addAttribute("peliculasDisponibles", peliculasDisponibles);
            model.addAttribute("misFavoritos", misFavoritos);
            model.addAttribute("misResenas", misResenas);

            System.out.println("✅ Dashboard Cliente cargado:");
            System.out.println("   - Usuario: " + usuario.getNombreCompleto());
            System.out.println("   - Películas disponibles: " + totalPeliculasDisponibles);
            System.out.println("   - Favoritos: " + totalFavoritos);
            System.out.println("   - Reseñas: " + totalResenasUsuario);

        } catch (Exception e) {
            System.err.println("❌ Error cargando dashboard cliente: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "No se pudo cargar el dashboard");
        }

        return "cliente-dashboard";
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

            return "cliente/peliculas"; // Vista: cliente/peliculas.html
        } catch (Exception e) {
            System.err.println("Error cargando películas: " + e.getMessage());
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

            return "cliente/favoritos"; // Vista: cliente/favoritos.html
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

            return "cliente/mis-resenas"; // Vista: cliente/mis-resenas.html
        } catch (Exception e) {
            System.err.println("Error cargando reseñas: " + e.getMessage());
            return "redirect:/cliente/dashboard";
        }
    }
}