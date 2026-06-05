package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.dto.UsuarioResenaDTO;
import com.NetPelis.netPelis.repository.RepositorioFavorito;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.repository.RepositorioResena;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import com.NetPelis.netPelis.service.impl.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
public class AdminReportesController {

    private final RepositorioResena repositorioResena;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPelicula repositorioPelicula;
    private final RepositorioFavorito repositorioFavorito;
    private final ReporteService reporteService;

    /**
     * Dashboard de reportes
     */
    @GetMapping
    public String dashboardReportes(Model model) {
        model.addAttribute("totalPeliculas", repositorioPelicula.count());
        model.addAttribute("totalUsuarios", repositorioUsuario.count());
        model.addAttribute("totalResenas", repositorioResena.count());
        model.addAttribute("totalFavoritos", repositorioFavorito.count());
        return "admin/reportes-dashboard";
    }

    /**
     * Reporte: Usuarios con más reseñas
     */
    @GetMapping("/usuarios-activos")
    public String usuariosConMasResenas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            Page<UsuarioResenaDTO> pagina = reporteService.getUsuariosConMasResenas(page, size);

            model.addAttribute("reporteUsuarios", pagina.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pagina.getTotalPages());
            model.addAttribute("totalElements", pagina.getTotalElements());
            model.addAttribute("size", size);

            // Stats adicionales
            model.addAttribute("totalPeliculas", repositorioPelicula.count());
            model.addAttribute("totalUsuarios", repositorioUsuario.count());
            model.addAttribute("totalResenas", repositorioResena.count());
            model.addAttribute("totalFavoritos", repositorioFavorito.count());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar reporte: " + e.getMessage());
            model.addAttribute("reporteUsuarios", List.of());
        }

        return "admin/usuarios-activos";
    }

    /**
     * Exportar reporte a Excel
     */
    @GetMapping("/usuarios-activos/exportar")
    @ResponseBody
    public ResponseEntity<byte[]> exportarReporteExcel() {
        try {
            byte[] excelData = reporteService.exportarAExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "usuarios_activos_" + System.currentTimeMillis() + ".xlsx");

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}