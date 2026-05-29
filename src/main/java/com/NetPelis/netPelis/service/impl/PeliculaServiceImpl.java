package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.EstadoPelicula;
import com.NetPelis.netPelis.repository.RepositorioPelicula;
import com.NetPelis.netPelis.service.ArchivoStorageService;
import com.NetPelis.netPelis.service.PeliculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PeliculaServiceImpl implements PeliculaService {

    private final RepositorioPelicula repositorioPelicula;
    private final ArchivoStorageService archivoStorageService;

    @Override
    public List<Pelicula> obtenerTodas() {
        return repositorioPelicula.findAll();
    }

    @Override
    public Optional<Pelicula> obtenerPorIdConGeneros(Long id) {
        return repositorioPelicula.findByIdWithGeneros(id);
    }

    @Override
    public List<Pelicula> obtenerRecientesDashboard() {
        return repositorioPelicula.findRecientesConGeneros();
    }

    @Override
    @Transactional
    public Pelicula guardarPelicula(Pelicula pelicula, MultipartFile posterFile) throws IOException {
        // Si es nueva, establecer fecha de creación
        if (pelicula.getId() == null) {
            pelicula.setFechaCreacion(LocalDateTime.now());
        }

        // Si se subió nueva imagen, procesarla
        if (posterFile != null && !posterFile.isEmpty()) {
            // Eliminar imagen anterior si existe
            if (pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isEmpty()) {
                archivoStorageService.eliminarImagen(pelicula.getPosterUrl());
            }

            // Guardar nueva imagen y obtener URL
            String posterUrl = archivoStorageService.guardarImagen(posterFile, "poster");
            pelicula.setPosterUrl(posterUrl);
        }

        // Si no hay imagen y es nueva, establecer placeholder
        if (pelicula.getId() == null &&
                (pelicula.getPosterUrl() == null || pelicula.getPosterUrl().isEmpty())) {
            pelicula.setPosterUrl("/img/poster-default.jpg");
        }

        return repositorioPelicula.save(pelicula);
    }

    @Override
    @Transactional
    public void eliminarPelicula(Long id) throws IOException {
        Pelicula pelicula = repositorioPelicula.findById(id)
                .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));

        // Eliminar imagen del sistema de archivos
        if (pelicula.getPosterUrl() != null &&
                !pelicula.getPosterUrl().isEmpty() &&
                !pelicula.getPosterUrl().startsWith("/img/")) {
            archivoStorageService.eliminarImagen(pelicula.getPosterUrl());
        }

        repositorioPelicula.delete(pelicula);
    }

    @Override
    public long contarPorEstado(EstadoPelicula estado) {
        return repositorioPelicula.countByEstado(estado);
    }

    @Override
    public List<Pelicula> buscar(String titulo, Integer anio, EstadoPelicula estado) {
        return repositorioPelicula.buscarAvanzado(titulo, anio, estado);
    }
}