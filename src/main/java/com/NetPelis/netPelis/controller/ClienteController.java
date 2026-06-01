package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Favorito;
import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.Resena;
import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioResena repositorioResena;

    /**
     * Agregar/Quitar película de favoritos (AJAX)
     */
    @PostMapping("/favoritos/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleFavorito(@RequestParam Long peliculaId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String email = auth.getName();
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            Optional<Pelicula> peliculaOpt = repositorioPelicula.findById(peliculaId);
            if (peliculaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Película no encontrada"));
            }

            boolean existe = repositorioFavorito.existsByUsuarioIdAndPeliculaId(usuario.getId(), peliculaId);

            if (existe) {
                repositorioFavorito.deleteByUsuarioIdAndPeliculaId(usuario.getId(), peliculaId);
                return ResponseEntity.ok(Map.of("success", true, "message", "Eliminado de favoritos", "isFavorite", false));
            } else {
                Favorito favorito = new Favorito();
                favorito.setUsuario(usuario);
                favorito.setPelicula(peliculaOpt.get());
                favorito.setFechaAgregado(LocalDateTime.now());
                repositorioFavorito.save(favorito);
                return ResponseEntity.ok(Map.of("success", true, "message", "Agregado a favoritos", "isFavorite", true));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    /**
     * Verificar si una película es favorita (AJAX)
     */
    @GetMapping("/favoritos/check/{peliculaId}")
    @ResponseBody
    public ResponseEntity<?> checkFavorito(@PathVariable Long peliculaId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String email = auth.getName();
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            boolean esFavorito = repositorioFavorito.existsByUsuarioIdAndPeliculaId(usuario.getId(), peliculaId);
            return ResponseEntity.ok(Map.of("isFavorite", esFavorito));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Obtener cantidad de favoritos del usuario
     */
    @GetMapping("/api/favoritos/count")
    @ResponseBody
    public ResponseEntity<?> contarFavoritos() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String email = auth.getName();
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            long count = repositorioFavorito.countByUsuarioId(usuario.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Crear o actualizar reseña
     */
    @PostMapping("/resenas/crear")
    public String crearResena(
            @RequestParam Long peliculaId,
            @RequestParam String titulo,
            @RequestParam String texto,
            @RequestParam String puntuacion,
            @RequestParam(required = false) Boolean esRecomendada,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Optional<Pelicula> peliculaOpt = repositorioPelicula.findById(peliculaId);
            if (peliculaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Película no encontrada");
                return "cliente-dashboard";
            }

            BigDecimal puntuacionDecimal = new BigDecimal(puntuacion);
            if (puntuacionDecimal.compareTo(BigDecimal.ZERO) < 0 || puntuacionDecimal.compareTo(new BigDecimal("10")) > 0) {
                redirectAttributes.addFlashAttribute("error", "La puntuación debe estar entre 0 y 10");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "cliente-dashboard";
            }

            // Verificar si ya existe reseña
            Optional<Resena> resenaExistente = repositorioResena.findByUsuarioIdAndPeliculaId(usuario.getId(), peliculaId);

            Resena resena;
            if (resenaExistente.isPresent()) {
                // Actualizar reseña existente
                resena = resenaExistente.get();
                resena.setTitulo(titulo);
                resena.setTexto(texto);
                resena.setPuntuacion(puntuacionDecimal);
                resena.setEsRecomendada(esRecomendada != null ? esRecomendada : true);
                resena.setFechaActualizacion(LocalDateTime.now());
            } else {
                // Crear nueva reseña
                resena = new Resena();
                resena.setUsuario(usuario);
                resena.setPelicula(peliculaOpt.get());
                resena.setTitulo(titulo);
                resena.setTexto(texto);
                resena.setPuntuacion(puntuacionDecimal);
                resena.setEsRecomendada(esRecomendada != null ? esRecomendada : true);
                resena.setFechaCreacion(LocalDateTime.now());
            }

            repositorioResena.save(resena);
            redirectAttributes.addFlashAttribute("mensaje", "Reseña guardada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Puntuación inválida. Usa formato decimal (ej: 8.5)");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "cliente-dashboard";
    }

    /**
     * Eliminar reseña
     */
    @PostMapping("/resenas/eliminar/{id}")
    public String eliminarResena(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            Usuario usuario = repositorioUsuario.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Optional<Resena> resenaOpt = repositorioResena.findById(id);
            if (resenaOpt.isPresent() && resenaOpt.get().getUsuario().getId().equals(usuario.getId())) {
                repositorioResena.delete(resenaOpt.get());
                redirectAttributes.addFlashAttribute("mensaje", "Reseña eliminada");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta reseña");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/cliente/dashboard";
    }
}