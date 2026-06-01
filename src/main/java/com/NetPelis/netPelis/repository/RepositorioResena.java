package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.dto.UsuarioResenaDTO;
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

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula ORDER BY r.fechaCreacion DESC")
    List<Resena> findAllWithUsuarioAndPelicula();

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula WHERE r.id = :id")
    Optional<Resena> findByIdWithUsuarioAndPelicula(@Param("id") Long id);

    @Query("SELECT r FROM Resena r JOIN r.usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByUsuarioEmailContaining(@Param("email") String email);

    @Query("SELECT r FROM Resena r JOIN r.pelicula p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByPeliculaTituloContaining(@Param("titulo") String titulo);

    long countByEsRecomendadaTrue();

    Optional<Resena> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    // ══════════════════════════════════════
    // 📊 MÉTODOS PARA REPORTE - CORREGIDOS
    // ══════════════════════════════════════

    /**
     * Obtener usuarios con más reseñas (TOP N)
     * ✅ CORREGIDO: AVG directo sobre BigDecimal + sin LIMIT en constructor expression
     */
    @Query("SELECT new com.NetPelis.netPelis.dto.UsuarioResenaDTO(" +
            "u.id, u.nombreCompleto, u.email, COUNT(r.id), AVG(r.puntuacion)) " +
            "FROM Resena r " +
            "JOIN r.usuario u " +
            "GROUP BY u.id, u.nombreCompleto, u.email " +
            "ORDER BY COUNT(r.id) DESC")
    List<UsuarioResenaDTO> findUsuariosConMasResenas();

    /**
     * Obtener TOP N usuarios con más reseñas
     * ✅ CORREGIDO: Usar Pageable para limitar resultados
     */
    @Query("SELECT new com.NetPelis.netPelis.dto.UsuarioResenaDTO(" +
            "u.id, u.nombreCompleto, u.email, COUNT(r.id), AVG(r.puntuacion)) " +
            "FROM Resena r " +
            "JOIN r.usuario u " +
            "GROUP BY u.id, u.nombreCompleto, u.email " +
            "ORDER BY COUNT(r.id) DESC")
    List<UsuarioResenaDTO> findTopUsuariosConMasResenas(@Param("limit") int limit);
}