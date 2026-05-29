package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Favorito;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para gestión de películas (ADMIN)
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
    private final RepositorioResena repositorioResena;  // ✅ AGREGAR ESTE CAMPO

    /**
     * ✅ Lista todas las películas CON FILTROS Y PAGINACIÓN
     * RUTA: GET /admin/peliculas
     * TEMPLATE: templates/admin/peliculas.html
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

            // ✅ CORRECCIÓN: Obtener valores REALES de la BD
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());  // ✅ Si tienes este método
            model.addAttribute("totalResenas", repositorioResena.count());                // ✅ AGREGADO
            model.addAttribute("totalFavoritos", repositorioFavorito.count());            // ✅ AGREGADO

            model.addAttribute("peliculas", peliculas);
            model.addAttribute("todosLosGeneros", repositorioGenero.findAll());

            System.out.println("✅ Página de películas cargada:");
            System.out.println("   - Películas: " + peliculas.size());
            System.out.println("   - Usuarios: " + repositorioUsuario.countByActivoTrue());
            System.out.println("   - Reseñas: " + repositorioResena.count());
            System.out.println("   - Favoritos: " + repositorioFavorito.count());

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("peliculas", List.of());
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalResenas", 0);
            model.addAttribute("totalFavoritos", 0);
        }

        return "admin/peliculas";
    }


    /**
     * API REST: Obtiene una película por ID en formato JSON (para el modal de edición)
     * RUTA: GET /admin/peliculas/api/{id}
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
     * RUTA: POST /admin/peliculas/guardar
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
            @RequestParam(value = "generos", required = false) List<Long> generosIds,
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

            // Asignar géneros si se proporcionaron
            if (generosIds != null && !generosIds.isEmpty()) {
                Set<Genero> generosSet = new HashSet<>();
                for (Long generoId : generosIds) {
                    repositorioGenero.findById(generoId).ifPresent(generosSet::add);
                }
                pelicula.setGeneros(generosSet);
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

        return "redirect:/admin/peliculas";  // ✅ Redirige a la página de películas
    }

    /**
     * API REST: Upload de imagen vía AJAX (para preview en tiempo real)
     * RUTA: POST /admin/peliculas/api/upload-imagen
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
     * RUTA: POST /admin/peliculas/{id}/eliminar
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
        return "redirect:/admin/peliculas";  // ✅ Redirige a la página de películas
    }

    /**
     * API REST: Buscar películas con filtros
     * RUTA: GET /admin/peliculas/api/buscar
     */
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<Pelicula>> buscarPeliculasApi(
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "estado", required = false) EstadoPelicula estado) {

        List<Pelicula> peliculas = peliculaService.buscar(titulo, anio, estado);
        return ResponseEntity.ok(peliculas);
    }

    @GetMapping("/favoritos")
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

            System.out.println("✅ Página de favoritos cargada: " + favoritos.size() + " registros");

        } catch (Exception e) {
            System.err.println("❌ Error cargando favoritos: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("favoritos", List.of());
        }

        return "admin/favoritos";  // Busca: templates/admin/favoritos.html
    }

    /**
     * Eliminar un favorito (ADMIN)
     * RUTA: POST /admin/favoritos/{id}/eliminar
     */
    @PostMapping("/favoritos/{id}/eliminar")
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
    @GetMapping("/favoritos/api/{id}")
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