package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Favorito;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para gestión de FAVORITOS en panel ADMIN
 * RUTAS: /admin/favoritos/*
 */
@Controller
@RequestMapping("/admin/favoritos")
@RequiredArgsConstructor
public class AdminFavoritoController {

    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;

    /**
     * Lista todos los favoritos con información de usuario y película
     * RUTA: GET /admin/favoritos
     * TEMPLATE: templates/admin/favoritos.html
     */
    @GetMapping
    public String listarFavoritos(
            @RequestParam(value = "usuario", required = false) String filtroUsuario,
            @RequestParam(value = "pelicula", required = false) String filtroPelicula,
            @RequestParam(value = "desde", required = false) String filtroDesde,
            @RequestParam(value = "hasta", required = false) String filtroHasta,
            Model model) {

        try {
            // Obtener todos los favoritos con JOIN para traer usuario y película
            List<Favorito> favoritos = repositorioFavorito.findAllWithUsuarioAndPelicula();

            // Aplicar filtros si existen
            if (filtroUsuario != null && !filtroUsuario.trim().isEmpty()) {
                favoritos = favoritos.stream()
                        .filter(f -> f.getUsuario().getNombreCompleto().toLowerCase().contains(filtroUsuario.toLowerCase()) ||
                                f.getUsuario().getEmail().toLowerCase().contains(filtroUsuario.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (filtroPelicula != null && !filtroPelicula.trim().isEmpty()) {
                favoritos = favoritos.stream()
                        .filter(f -> f.getPelicula().getTitulo().toLowerCase().contains(filtroPelicula.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filtros por fecha (opcional)
            if (filtroDesde != null && !filtroDesde.trim().isEmpty()) {
                LocalDateTime desde = LocalDateTime.parse(filtroDesde + "T00:00:00");
                favoritos = favoritos.stream()
                        .filter(f -> !f.getFechaAgregado().isBefore(desde))
                        .collect(Collectors.toList());
            }

            if (filtroHasta != null && !filtroHasta.trim().isEmpty()) {
                LocalDateTime hasta = LocalDateTime.parse(filtroHasta + "T23:59:59");
                favoritos = favoritos.stream()
                        .filter(f -> !f.getFechaAgregado().isAfter(hasta))
                        .collect(Collectors.toList());
            }

            // Mantener valores de filtros en el modelo
            model.addAttribute("filtroUsuario", filtroUsuario);
            model.addAttribute("filtroPelicula", filtroPelicula);
            model.addAttribute("filtroDesde", filtroDesde);
            model.addAttribute("filtroHasta", filtroHasta);

            // Stats para sidebar (reutilizar existentes)
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.count());

            // Lista de favoritos filtrados
            model.addAttribute("favoritos", favoritos);

            System.out.println("✅ Página de favoritos admin cargada: " + favoritos.size() + " registros");

        } catch (Exception e) {
            System.err.println("❌ Error cargando favoritos admin: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("favoritos", List.of());
        }

        return "admin/favoritos";  // Busca: templates/admin/favoritos.html
    }

    /**
     * Eliminar un favorito (ADMIN) - AJAX
     * RUTA: POST /admin/favoritos/{id}/eliminar
     */
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarFavoritoAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Favorito> favoritoOpt = repositorioFavorito.findById(id);
            if (favoritoOpt.isPresent()) {
                repositorioFavorito.delete(favoritoOpt.get());
                return ResponseEntity.ok(Map.of("success", true, "message", "Favorito eliminado"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Favorito no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Obtener favorito por ID (para modal de edición/ver)
     * RUTA: GET /admin/favoritos/api/{id}
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerFavoritoApi(@PathVariable Long id) {
        try {
            Optional<Favorito> favoritoOpt = repositorioFavorito.findByIdWithUsuarioAndPelicula(id);
            if (favoritoOpt.isPresent()) {
                Favorito f = favoritoOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", f.getId());
                response.put("usuario", Map.of(
                        "id", f.getUsuario().getId(),
                        "nombreCompleto", f.getUsuario().getNombreCompleto(),
                        "email", f.getUsuario().getEmail()
                ));
                response.put("pelicula", Map.of(
                        "id", f.getPelicula().getId(),
                        "titulo", f.getPelicula().getTitulo(),
                        "posterUrl", f.getPelicula().getPosterUrl(),
                        "anioLanzamiento", f.getPelicula().getAnioLanzamiento()
                ));
                response.put("fechaAgregado", f.getFechaAgregado());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}