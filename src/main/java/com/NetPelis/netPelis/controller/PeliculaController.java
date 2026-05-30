package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.EstadoPelicula;
import com.NetPelis.netPelis.entity.Genero;
import com.NetPelis.netPelis.repository.*;
import com.NetPelis.netPelis.service.PeliculaService;
import com.NetPelis.netPelis.service.ArchivoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

/**
 * Controller para gestión de PELÍCULAS (ADMIN)
 * RUTAS: /admin/peliculas/*
 */
@Controller
@RequestMapping("/admin/peliculas")
@RequiredArgsConstructor
public class PeliculaController {

    private final RepositorioUsuario repositorioUsuario;
    private final PeliculaService peliculaService;
    private final ArchivoStorageService archivoStorageService;
    private final RepositorioGenero repositorioGenero;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioResena repositorioResena;

    /**
     * Lista películas con filtros
     * RUTA: GET /admin/peliculas
     */
    @GetMapping
    public String listarPeliculas(
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "estado", required = false) EstadoPelicula estado,
            Model model) {

        try {
            List<Pelicula> peliculas = peliculaService.buscar(titulo, anio, estado);

            model.addAttribute("filtroTitulo", titulo);
            model.addAttribute("filtroAnio", anio);
            model.addAttribute("filtroEstado", estado != null ? estado.name() : null);

            // ✅ Stats del SISTEMA para sidebar
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalResenas", repositorioResena.count());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());

            model.addAttribute("peliculas", peliculas);
            model.addAttribute("todosLosGeneros", repositorioGenero.findAll());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("peliculas", List.of());
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalResenas", 0);
            model.addAttribute("totalFavoritos", 0);
        }

        return "admin/peliculas";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerPeliculaApi(@PathVariable Long id) {
        Optional<Pelicula> pelicula = peliculaService.obtenerPorIdConGeneros(id);
        return pelicula.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/guardar")
    public String guardarPelicula(
            @RequestParam(value = "peliculaId", required = false) Long peliculaId,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "sinopsis", required = false) String sinopsis,
            @RequestParam("anioLanzamiento") Integer anioLanzamiento,
            @RequestParam(value = "duracionMinutos", required = false) Integer duracionMinutos,
            @RequestParam(value = "clasificacion", required = false) String clasificacion,
            @RequestParam(value = "director", required = false) String director,
            @RequestParam(value = "trailerUrl", required = false) String trailerUrl,
            @RequestParam(value = "posterUrl", required = false) String posterUrl,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "generos", required = false) List<Long> generosIds,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            RedirectAttributes redirectAttributes) {

        try {
            Pelicula pelicula;
            if (peliculaId != null && peliculaId > 0) {
                pelicula = peliculaService.obtenerPorIdConGeneros(peliculaId)
                        .orElseThrow(() -> new RuntimeException("Película no encontrada"));
            } else {
                pelicula = new Pelicula();
            }

            pelicula.setTitulo(titulo);
            pelicula.setSinopsis(sinopsis);
            pelicula.setAnioLanzamiento(anioLanzamiento);
            pelicula.setDuracionMinutos(duracionMinutos);
            pelicula.setClasificacion(clasificacion);
            pelicula.setDirector(director);
            pelicula.setTrailerUrl(trailerUrl);

            if (posterFile != null && !posterFile.isEmpty()) {
                pelicula.setPosterUrl(null);
            } else if (posterUrl != null && !posterUrl.trim().isEmpty()) {
                pelicula.setPosterUrl(posterUrl.trim());
            }

            if (estado != null && !estado.trim().isEmpty()) {
                pelicula.setEstado(EstadoPelicula.valueOf(estado.trim().toUpperCase()));
            } else {
                pelicula.setEstado(EstadoPelicula.PUBLICADO);
            }

            if (generosIds != null && !generosIds.isEmpty()) {
                Set<Genero> generosSet = new HashSet<>();
                for (Long generoId : generosIds) {
                    repositorioGenero.findById(generoId).ifPresent(generosSet::add);
                }
                pelicula.setGeneros(generosSet);
            }

            peliculaService.guardarPelicula(pelicula, posterFile);
            redirectAttributes.addFlashAttribute("mensaje", "Película guardada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error con la imagen: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/admin/peliculas";
    }

    @PostMapping("/api/upload-imagen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImagenApi(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Archivo vacío");
                return ResponseEntity.badRequest().body(response);
            }
            String url = archivoStorageService.guardarImagen(file, "poster");
            response.put("success", true);
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarPelicula(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            peliculaService.eliminarPelicula(id);
            redirectAttributes.addFlashAttribute("mensaje", "Película eliminada");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/admin/peliculas";
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<Pelicula>> buscarPeliculasApi(
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "estado", required = false) EstadoPelicula estado) {
        return ResponseEntity.ok(peliculaService.buscar(titulo, anio, estado));
    }
}