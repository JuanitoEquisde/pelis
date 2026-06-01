package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.entity.Genero;
import com.NetPelis.netPelis.repository.RepositorioGenero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/generos")
@RequiredArgsConstructor
public class AdminGeneroController {

    private final RepositorioGenero repositorioGenero;

    /**
     * Listar géneros con paginación
     */
    @GetMapping
    public String listarGeneros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            Page<Genero> generosPage = repositorioGenero.findAll(pageable);

            model.addAttribute("generos", generosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", generosPage.getTotalPages());
            model.addAttribute("totalElements", generosPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sortBy);

            // Stats para el sidebar
            model.addAttribute("totalGeneros", generosPage.getTotalElements());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los géneros: " + e.getMessage());
        }

        return "admin/generos";
    }

    /**
     * Crear nuevo género
     */
    @PostMapping("/crear")
    public String crearGenero(
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes) {

        try {
            // Verificar si ya existe
            if (repositorioGenero.existsByNombreIgnoreCase(nombre)) {
                redirectAttributes.addFlashAttribute("error", "El género '" + nombre + "' ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/admin/generos";
            }

            Genero genero = new Genero();
            genero.setNombre(nombre.trim().toUpperCase());
            repositorioGenero.save(genero);

            redirectAttributes.addFlashAttribute("mensaje", "Género creado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/generos";
    }

    /**
     * Actualizar género
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarGenero(
            @PathVariable Long id,
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes) {

        try {
            Genero genero = repositorioGenero.findById(id)
                    .orElseThrow(() -> new RuntimeException("Género no encontrado"));

            // Verificar si otro género ya tiene ese nombre
            if (repositorioGenero.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
                redirectAttributes.addFlashAttribute("error", "El género '" + nombre + "' ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/admin/generos";
            }

            genero.setNombre(nombre.trim().toUpperCase());
            repositorioGenero.save(genero);

            redirectAttributes.addFlashAttribute("mensaje", "Género actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/generos";
    }

    /**
     * Eliminar género
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarGenero(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            if (!repositorioGenero.existsById(id)) {
                redirectAttributes.addFlashAttribute("error", "Género no encontrado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/admin/generos";
            }

            repositorioGenero.deleteById(id);

            redirectAttributes.addFlashAttribute("mensaje", "Género eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/generos";
    }
}