package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.EstadoPelicula;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/peliculas")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;
    private final ArchivoStorageService archivoStorageService;

    /**
     * Lista todas las películas (vista administrativa)
     */
    @GetMapping
    public String listarPeliculas(Model model) {
        model.addAttribute("peliculas", peliculaService.obtenerTodas());
        return "admin/peliculas-lista";
    }

    /**
     * API REST: Obtiene una película por ID en formato JSON (para el modal de edición)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerPeliculaApi(@PathVariable Long id) {
        Optional<Pelicula> pelicula = peliculaService.obtenerPorIdConGeneros(id);
        return pelicula.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Guarda o actualiza una película desde el formulario del modal
     */
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
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            RedirectAttributes redirectAttributes) {

        try {
            Pelicula pelicula;

            // Determinar si es creación o actualización
            if (peliculaId != null && peliculaId > 0) {
                pelicula = peliculaService.obtenerPorIdConGeneros(peliculaId)
                        .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + peliculaId));
            } else {
                pelicula = new Pelicula();
            }

            // Asignar campos básicos
            pelicula.setTitulo(titulo);
            pelicula.setSinopsis(sinopsis);
            pelicula.setAnioLanzamiento(anioLanzamiento);
            pelicula.setDuracionMinutos(duracionMinutos);
            pelicula.setClasificacion(clasificacion);
            pelicula.setDirector(director);
            pelicula.setTrailerUrl(trailerUrl);

            // Lógica de portada: prioriza archivo subido, si no existe usa URL externa
            if (posterFile != null && !posterFile.isEmpty()) {
                // El servicio se encargará de guardar la imagen y asignar la URL
                pelicula.setPosterUrl(null);
            } else if (posterUrl != null && !posterUrl.trim().isEmpty()) {
                pelicula.setPosterUrl(posterUrl.trim());
            }

            // Asignar estado
            if (estado != null && !estado.trim().isEmpty()) {
                pelicula.setEstado(EstadoPelicula.valueOf(estado.trim().toUpperCase()));
            } else {
                pelicula.setEstado(EstadoPelicula.PUBLICADO);
            }

            // Guardar en base de datos (con manejo de imagen)
            peliculaService.guardarPelicula(pelicula, posterFile);

            redirectAttributes.addFlashAttribute("mensaje", "Película guardada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar la imagen: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Estado inválido: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error inesperado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            e.printStackTrace();
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * API REST: Upload de imagen vía AJAX (para preview en tiempo real)
     */
    @PostMapping("/api/upload-imagen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImagenApi(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            String url = archivoStorageService.guardarImagen(file, "poster");

            response.put("success", true);
            response.put("url", url);
            response.put("message", "Imagen subida exitosamente");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error al subir: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Elimina una película y su imagen asociada
     */
    @PostMapping("/{id}/eliminar")
    public String eliminarPelicula(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            peliculaService.eliminarPelicula(id);
            redirectAttributes.addFlashAttribute("mensaje", "Película eliminada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }
}