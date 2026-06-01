package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioResena repositorioResena;

    @GetMapping
    public String listarUsuarios(
            @RequestParam(value = "id", required = false) Long filtroId,
            @RequestParam(value = "nombre", required = false) String filtroNombre,
            @RequestParam(value = "email", required = false) String filtroEmail,
            @RequestParam(value = "rol", required = false) RolUsuario filtroRol,
            @RequestParam(value = "activo", required = false) Boolean filtroActivo,
            Model model) {

        try {
            List<Usuario> usuarios = repositorioUsuario.buscarUsuarios(
                    filtroId, filtroNombre, filtroEmail, filtroRol, filtroActivo);

            model.addAttribute("usuarios", usuarios);
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());
            model.addAttribute("totalResenas", repositorioResena.count());

            model.addAttribute("filtroId", filtroId);
            model.addAttribute("filtroNombre", filtroNombre);
            model.addAttribute("filtroEmail", filtroEmail);
            model.addAttribute("filtroRol", filtroRol != null ? filtroRol.name() : null);
            model.addAttribute("filtroActivo", filtroActivo);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("usuarios", List.of());
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalFavoritos", 0);
            model.addAttribute("totalResenas", 0);
        }

        return "admin/usuarios";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerUsuarioApi(@PathVariable Long id) {
        try {
            return repositorioUsuario.findById(id)
                    .map(u -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("id", u.getId());
                        response.put("nombreCompleto", u.getNombreCompleto());
                        response.put("email", u.getEmail());
                        response.put("rol", u.getRol().name());
                        response.put("activo", u.getActivo());
                        response.put("fechaRegistro", u.getFechaRegistro());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            if (repositorioUsuario.existsById(id)) {
                repositorioUsuario.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Usuario eliminado"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        try {
            Usuario usuario = repositorioUsuario.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setActivo(activo);
            repositorioUsuario.save(usuario);
            return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    /**
     * Actualizar usuario completo
     */
    @PutMapping("/{id}/actualizar")
    @ResponseBody
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> datos) {
        try {
            Usuario usuario = repositorioUsuario.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Actualizar campos
            if (datos.containsKey("nombreCompleto")) {
                usuario.setNombreCompleto((String) datos.get("nombreCompleto"));
            }
            if (datos.containsKey("email")) {
                usuario.setEmail((String) datos.get("email"));
            }
            if (datos.containsKey("rol")) {
                usuario.setRol(RolUsuario.valueOf((String) datos.get("rol")));
            }
            if (datos.containsKey("activo")) {
                usuario.setActivo((Boolean) datos.get("activo"));
            }

            repositorioUsuario.save(usuario);
            return ResponseEntity.ok(Map.of("success", true, "message", "Usuario actualizado"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Formulario de actualización (para Thymeleaf)
     */
    @PostMapping("/{id}/actualizar-form")
    public String actualizarUsuarioForm(
            @PathVariable Long id,
            @RequestParam String nombreCompleto,
            @RequestParam String email,
            @RequestParam String rol,
            @RequestParam Boolean activo,
            RedirectAttributes   redirectAttributes) {
        try {
            Usuario usuario = repositorioUsuario.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setNombreCompleto(nombreCompleto);
            usuario.setEmail(email);
            usuario.setRol(RolUsuario.valueOf(rol));
            usuario.setActivo(activo);

            repositorioUsuario.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/usuarios";
    }
}