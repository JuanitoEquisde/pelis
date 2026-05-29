package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Pelicula;
import com.NetPelis.netPelis.entity.EstadoPelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPelicula extends JpaRepository<Pelicula, Long> {

    // Buscar por estado
    List<Pelicula> findByEstado(EstadoPelicula estado);

    // Buscar por título (búsqueda parcial, case-insensitive)
    List<Pelicula> findByTituloContainingIgnoreCase(String titulo);

    // Buscar por director
    List<Pelicula> findByDirectorContainingIgnoreCase(String director);

    // Buscar por año
    List<Pelicula> findByAnioLanzamiento(Integer anio);

    // Películas recientes (últimas 10 publicadas)
    @Query("SELECT p FROM Pelicula p WHERE p.estado = :estado ORDER BY p.fechaCreacion DESC LIMIT 10")
    List<Pelicula> findRecientesPublicadas(@Param("estado") EstadoPelicula estado);

    // Contar películas por estado
    long countByEstado(EstadoPelicula estado);

    // Buscar con géneros (fetch join para evitar N+1)
    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.generos WHERE p.id = :id")
    Optional<Pelicula> findByIdWithGeneros(@Param("id") Long id);

    // Listar con géneros para dashboard
    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.generos ORDER BY p.fechaCreacion DESC LIMIT 10")
    List<Pelicula> findRecientesConGeneros();

    // Búsqueda avanzada
    @Query("SELECT p FROM Pelicula p WHERE " +
            "(:titulo IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
            "(:anio IS NULL OR p.anioLanzamiento = :anio) AND " +
            "(:estado IS NULL OR p.estado = :estado) " +
            "ORDER BY p.fechaCreacion DESC")
    List<Pelicula> buscarAvanzado(@Param("titulo") String titulo,
                                  @Param("anio") Integer anio,
                                  @Param("estado") EstadoPelicula estado);

    List<Pelicula> findByTituloContainingIgnoreCaseAndEstado(String titulo, EstadoPelicula estado);

}