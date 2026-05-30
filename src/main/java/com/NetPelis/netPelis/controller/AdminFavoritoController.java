package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Favorito;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para gestión de FAVORITOS (ADMIN)
 * RUTAS: /admin/favoritos/*
 */
@Controller
@RequestMapping("/admin/favoritos")
@RequiredArgsConstructor
public class AdminFavoritoController {

    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioResena repositorioResena;

    /**
     * Lista favoritos con filtros
     * RUTA: GET /admin/favoritos
     */
    @GetMapping
    public String listarFavoritos(
            @RequestParam(value = "usuario", required = false) String filtroUsuario,
            @RequestParam(value = "pelicula", required = false) String filtroPelicula,
            @RequestParam(value = "desde", required = false) String filtroDesde,
            @RequestParam(value = "hasta", required = false) String filtroHasta,
            Model model) {

        try {
            List<Favorito> favoritos = repositorioFavorito.findAllWithUsuarioAndPelicula();

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

            model.addAttribute("filtroUsuario", filtroUsuario);
            model.addAttribute("filtroPelicula", filtroPelicula);
            model.addAttribute("filtroDesde", filtroDesde);
            model.addAttribute("filtroHasta", filtroHasta);

            // ✅ Stats del SISTEMA para sidebar
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalResenas", repositorioResena.count());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());  // ✅ Total del sistema

            model.addAttribute("favoritos", favoritos);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("favoritos", List.of());
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalResenas", 0);
            model.addAttribute("totalFavoritos", 0);
        }

        return "admin/favoritos";
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarFavorito(@PathVariable Long id) {
        try {
            if (repositorioFavorito.existsById(id)) {
                repositorioFavorito.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Favorito eliminado"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "No encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerFavoritoApi(@PathVariable Long id) {
        try {
            return repositorioFavorito.findByIdWithUsuarioAndPelicula(id)
                    .map(f -> {
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
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}