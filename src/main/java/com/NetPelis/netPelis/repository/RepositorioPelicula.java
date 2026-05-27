package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepositorioPelicula extends JpaRepository<Pelicula, Long> {
    List<Pelicula> findByEstado(String estado);
    List<Pelicula> findByTituloContainingIgnoreCase(String titulo);
}