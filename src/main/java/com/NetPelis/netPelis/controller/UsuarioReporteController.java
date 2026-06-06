package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.dto.UsuariosConMasResenasDTO;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import com.NetPelis.netPelis.service.impl.UsuarioReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reportes/usuarios-resenas")
@RequiredArgsConstructor
public class UsuarioReporteController {

    // ✅ Inyección automática con Lombok (no necesitas @Autowired)
    private final UsuarioReporteService usuarioReporteService;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioFavorito repositorioFavorito;
    private final RepositorioResena repositorioResena;

    /**
     * ✅ Carga las estadísticas para el sidebar en TODAS las vistas de este controller
     */
    @ModelAttribute
    public void agregarAtributosComunes(Model model) {
        try {
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.countByActivoTrue());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());
            model.addAttribute("totalResenas", repositorioResena.count());
        } catch (Exception e) {
            // Valores por defecto en caso de error
            model.addAttribute("totalPeliculas", 0);
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalFavoritos", 0);
            model.addAttribute("totalResenas", 0);
        }
    }

    /**
     * Lista usuarios con más reseñas (con paginación y búsqueda)
     */
    @GetMapping
    public String listarUsuariosConMasResenas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(required = false) String busqueda,
            Model model) {

        try {
            Page<UsuariosConMasResenasDTO> paginaUsuarios;

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                paginaUsuarios = usuarioReporteService.buscarUsuariosConMasResenas(busqueda, pagina, tamano);
                model.addAttribute("busqueda", busqueda);
            } else {
                paginaUsuarios = usuarioReporteService.obtenerUsuariosConMasResenas(pagina, tamano);
            }

            model.addAttribute("usuarios", paginaUsuarios);
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("tamanoPagina", tamano);
            model.addAttribute("totalPaginas", paginaUsuarios.getTotalPages());
            model.addAttribute("totalElementos", paginaUsuarios.getTotalElements());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el reporte: " + e.getMessage());
        }

        return "admin/usuarios-con-mas-resenas";
    }
}