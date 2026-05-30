package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioResena extends JpaRepository<Resena, Long> {

    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario ORDER BY r.fechaCreacion DESC LIMIT :limit")
    List<Resena> findRecientesConUsuario(@Param("limit") int limit);

    long countByUsuarioId(Long usuarioId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Resena> findByPeliculaIdOrderByFechaCreacionDesc(Long peliculaId);

    // ✅ MÉTODOS FALTANTES - AGREGA ESTOS:

    /**
     * Obtener todas las reseñas con usuario y película (JOIN FETCH)
     */
    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula ORDER BY r.fechaCreacion DESC")
    List<Resena> findAllWithUsuarioAndPelicula();

    /**
     * Obtener una reseña específica con usuario y película
     */
    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula WHERE r.id = :id")
    Optional<Resena> findByIdWithUsuarioAndPelicula(@Param("id") Long id);

    /**
     * Buscar reseñas por email de usuario
     */
    @Query("SELECT r FROM Resena r JOIN r.usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByUsuarioEmailContaining(@Param("email") String email);

    /**
     * Buscar reseñas por título de película
     */
    @Query("SELECT r FROM Resena r JOIN r.pelicula p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByPeliculaTituloContaining(@Param("titulo") String titulo);

    /**
     * Contar reseñas recomendadas
     */
    long countByEsRecomendadaTrue();

    /**
     * Obtener reseña por usuario y película
     */
    Optional<Resena> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);
}