package com.NetPelis.netPelis.controller;

import com.NetPelis.netPelis.dto.UsuariosConMasResenasDTO;
import com.NetPelis.netPelis.service.impl.UsuarioReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reportes/usuarios-resenas")
public class UsuarioReporteController {

    @Autowired
    private UsuarioReporteService usuarioReporteService;

    @GetMapping
    public String listarUsuariosConMasResenas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(required = false) String busqueda,
            Model model) {

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

        return "admin/usuarios-con-mas-resenas";
    }
}