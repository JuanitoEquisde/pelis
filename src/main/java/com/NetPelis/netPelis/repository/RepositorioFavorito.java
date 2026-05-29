package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioFavorito extends JpaRepository<Favorito, Long> {
    long countByUsuarioId(Long usuarioId);

    List<Favorito> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    void deleteByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    // Agregar al final del archivo:

    /**
     * Obtener todos los favoritos con usuario y película (evita N+1)
     */
    @Query("SELECT f FROM Favorito f JOIN FETCH f.usuario JOIN FETCH f.pelicula ORDER BY f.fechaAgregado DESC")
    List<Favorito> findAllWithUsuarioAndPelicula();

    /**
     * Obtener un favorito específico con usuario y película
     */
    @Query("SELECT f FROM Favorito f JOIN FETCH f.usuario JOIN FETCH f.pelicula WHERE f.id = :id")
    Optional<Favorito> findByIdWithUsuarioAndPelicula(@Param("id") Long id);

    /**
     * Buscar favoritos por email de usuario (parcial)
     */
    @Query("SELECT f FROM Favorito f JOIN f.usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY f.fechaAgregado DESC")
    List<Favorito> findByUsuarioEmailContaining(@Param("email") String email);

    /**
     * Buscar favoritos por título de película (parcial)
     */
    @Query("SELECT f FROM Favorito f JOIN f.pelicula p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY f.fechaAgregado DESC")
    List<Favorito> findByPeliculaTituloContaining(@Param("titulo") String titulo);
}