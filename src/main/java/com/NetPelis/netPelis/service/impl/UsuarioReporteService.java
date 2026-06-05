package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.dto.UsuariosConMasResenasDTO;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioReporteService {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    public Page<UsuariosConMasResenasDTO> obtenerUsuariosConMasResenas(int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        return repositorioUsuario.findUsuariosConMasResenas(pageable);
    }

    public Page<UsuariosConMasResenasDTO> buscarUsuariosConMasResenas(String busqueda, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return obtenerUsuariosConMasResenas(pagina, tamano);
        }
        return repositorioUsuario.findUsuariosConMasResenasConBusqueda(busqueda, pageable);
    }

    public long contarUsuariosConResenas() {
        return repositorioUsuario.countUsuariosConResenas();
    }

    public long contarUsuariosConResenasConBusqueda(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return contarUsuariosConResenas();
        }
        return repositorioUsuario.countUsuariosConResenasConBusqueda(busqueda);
    }
}