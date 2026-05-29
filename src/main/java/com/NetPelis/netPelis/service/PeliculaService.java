package com.NetPelis.netPelis.service;

import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.EstadoPelicula;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface PeliculaService {

    List<Pelicula> obtenerTodas();

    Optional<Pelicula> obtenerPorIdConGeneros(Long id);

    List<Pelicula> obtenerRecientesDashboard();

    Pelicula guardarPelicula(Pelicula pelicula, MultipartFile posterFile) throws IOException;

    void eliminarPelicula(Long id) throws IOException;

    long contarPorEstado(EstadoPelicula estado);

    List<Pelicula> buscar(String titulo, Integer anio, EstadoPelicula estado);
}