package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Resena;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/resenas")
@RequiredArgsConstructor
public class AdminResenaController {

    private final RepositorioResena repositorioResena;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioFavorito repositorioFavorito;

    /**
     * Lista todas las reseñas con filtros
     * RUTA: GET /admin/resenas
     */
    @GetMapping
    public String listarResenas(
            @RequestParam(value = "usuario", required = false) String filtroUsuario,
            @RequestParam(value = "pelicula", required = false) String filtroPelicula,
            @RequestParam(value = "desde", required = false) String filtroDesde,
            @RequestParam(value = "hasta", required = false) String filtroHasta,
            @RequestParam(value = "minPuntuacion", required = false) Double minPuntuacion,
            @RequestParam(value = "recomendadas", required = false) Boolean soloRecomendadas,
            Model model) {

        try {
            List<Resena> resenas = repositorioResena.findAllWithUsuarioAndPelicula();

            // Filtro por usuario
            if (filtroUsuario != null && !filtroUsuario.trim().isEmpty()) {
                resenas = resenas.stream()
                        .filter(r -> r.getUsuario().getNombreCompleto().toLowerCase().contains(filtroUsuario.toLowerCase()) ||
                                r.getUsuario().getEmail().toLowerCase().contains(filtroUsuario.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filtro por película
            if (filtroPelicula != null && !filtroPelicula.trim().isEmpty()) {
                resenas = resenas.stream()
                        .filter(r -> r.getPelicula().getTitulo().toLowerCase().contains(filtroPelicula.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filtro por fecha desde
            if (filtroDesde != null && !filtroDesde.trim().isEmpty()) {
                LocalDateTime desde = LocalDateTime.parse(filtroDesde + "T00:00:00");
                resenas = resenas.stream()
                        .filter(r -> !r.getFechaCreacion().isBefore(desde))
                        .collect(Collectors.toList());
            }

            // Filtro por fecha hasta
            if (filtroHasta != null && !filtroHasta.trim().isEmpty()) {
                LocalDateTime hasta = LocalDateTime.parse(filtroHasta + "T23:59:59");
                resenas = resenas.stream()
                        .filter(r -> !r.getFechaCreacion().isAfter(hasta))
                        .collect(Collectors.toList());
            }

            // Filtro por puntuación mínima
            if (minPuntuacion != null) {
                resenas = resenas.stream()
                        .filter(r -> r.getPuntuacion().doubleValue() >= minPuntuacion)
                        .collect(Collectors.toList());
            }

            // Filtro solo recomendadas
            if (soloRecomendadas != null && soloRecomendadas) {
                resenas = resenas.stream()
                        .filter(Resena::getEsRecomendada)
                        .collect(Collectors.toList());
            }
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());  // ✅ Total del SISTEMA
            model.addAttribute("totalResenas", repositorioResena.count());      // ✅ Total del SISTEMA
            model.addAttribute("resenas", resenas);
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.count());
            model.addAttribute("totalResenas", resenas.size());
            model.addAttribute("filtroUsuario", filtroUsuario);
            model.addAttribute("filtroPelicula", filtroPelicula);
            model.addAttribute("filtroDesde", filtroDesde);
            model.addAttribute("filtroHasta", filtroHasta);
            model.addAttribute("minPuntuacion", minPuntuacion);
            model.addAttribute("soloRecomendadas", soloRecomendadas);

            System.out.println("✅ Reseñas cargadas: " + resenas.size());

        } catch (Exception e) {
            System.err.println("❌ Error cargando reseñas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("resenas", List.of());
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalResenas", 0);
        }

        return "admin/resenas";
    }

    /**
     * Eliminar reseña (AJAX)
     */
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarResena(@PathVariable Long id) {
        try {
            Optional<Resena> resenaOpt = repositorioResena.findById(id);
            if (resenaOpt.isPresent()) {
                repositorioResena.delete(resenaOpt.get());
                return ResponseEntity.ok(Map.of("success", true, "message", "Reseña eliminada"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Reseña no encontrada"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Obtener reseña por ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerResena(@PathVariable Long id) {
        try {
            Optional<Resena> resenaOpt = repositorioResena.findByIdWithUsuarioAndPelicula(id);
            if (resenaOpt.isPresent()) {
                Resena r = resenaOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", r.getId());
                response.put("titulo", r.getTitulo());
                response.put("texto", r.getTexto());
                response.put("puntuacion", r.getPuntuacion());
                response.put("esRecomendada", r.getEsRecomendada());
                response.put("fechaCreacion", r.getFechaCreacion());
                response.put("fechaActualizacion", r.getFechaActualizacion());
                response.put("usuario", Map.of(
                        "id", r.getUsuario().getId(),
                        "nombreCompleto", r.getUsuario().getNombreCompleto(),
                        "email", r.getUsuario().getEmail()
                ));
                response.put("pelicula", Map.of(
                        "id", r.getPelicula().getId(),
                        "titulo", r.getPelicula().getTitulo(),
                        "posterUrl", r.getPelicula().getPosterUrl(),
                        "anioLanzamiento", r.getPelicula().getAnioLanzamiento()
                ));
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Marcar reseña como leída/no leída (opcional)
     */
    @PostMapping("/{id}/marcar-leida")
    @ResponseBody
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        // Implementación opcional si quieres marcar reseñas como leídas
        return ResponseEntity.ok(Map.of("success", true));
    }
}