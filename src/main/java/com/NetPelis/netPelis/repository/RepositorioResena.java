package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.dto.UsuarioResenaDTO;
import com.NetPelis.netPelis.entity.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface RepositorioResena extends JpaRepository<Resena, Long> {

    // ══════════════════════════════════════
    // MÉTODOS BÁSICOS
    // ══════════════════════════════════════

    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    long countByUsuarioId(Long usuarioId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Resena> findByPeliculaIdOrderByFechaCreacionDesc(Long peliculaId);

    long countByEsRecomendadaTrue();

    Optional<Resena> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    // ══════════════════════════════════════
    // RESEÑAS CON JOIN FETCH
    // ══════════════════════════════════════

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario ORDER BY r.fechaCreacion DESC")
    List<Resena> findRecientesConUsuario(Pageable pageable);

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula ORDER BY r.fechaCreacion DESC")
    List<Resena> findAllWithUsuarioAndPelicula();

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula ORDER BY r.fechaCreacion DESC")
    Page<Resena> findAllWithUsuarioAndPelicula(Pageable pageable);

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario JOIN FETCH r.pelicula WHERE r.id = :id")
    Optional<Resena> findByIdWithUsuarioAndPelicula(@Param("id") Long id);

    // ══════════════════════════════════════
    // BÚSQUEDAS
    // ══════════════════════════════════════

    @Query("SELECT r FROM Resena r JOIN r.usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByUsuarioEmailContaining(@Param("email") String email);

    @Query("SELECT r FROM Resena r JOIN r.pelicula p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY r.fechaCreacion DESC")
    List<Resena> findByPeliculaTituloContaining(@Param("titulo") String titulo);

    // ══════════════════════════════════════
    // 📊 REPORTE USUARIOS ACTIVOS - NATIVE QUERIES
    // ══════════════════════════════════════

    /**
     * Retorna TODOS los usuarios con sus totales de reseñas (sin paginación)
     */
    @Query(value = "SELECT u.id AS usuarioId, " +
            "u.nombre_completo AS nombreCompleto, " +
            "u.email AS email, " +
            "COUNT(r.id) AS totalResenas, " +
            "COALESCE(AVG(r.puntuacion), 0) AS puntuacionPromedio, " +
            "MAX(r.fecha_creacion) AS ultimaResena, " +
            "u.fecha_registro AS fechaRegistro, " +
            "u.activo AS activo " +
            "FROM usuario u " +
            "LEFT JOIN resena r ON u.id = r.usuario_id " +
            "GROUP BY u.id, u.nombre_completo, u.email, u.fecha_registro, u.activo " +
            "ORDER BY totalResenas DESC",
            nativeQuery = true)
    List<Object[]> findUsuariosConMasResenas();

    /**
     * Retorna usuarios con sus totales de reseñas (con paginación)
     */
    @Query(value = "SELECT u.id AS usuarioId, " +
            "u.nombre_completo AS nombreCompleto, " +
            "u.email AS email, " +
            "COUNT(r.id) AS totalResenas, " +
            "COALESCE(AVG(r.puntuacion), 0) AS puntuacionPromedio, " +
            "MAX(r.fecha_creacion) AS ultimaResena, " +
            "u.fecha_registro AS fechaRegistro, " +
            "u.activo AS activo " +
            "FROM usuario u " +
            "LEFT JOIN resena r ON u.id = r.usuario_id " +
            "GROUP BY u.id, u.nombre_completo, u.email, u.fecha_registro, u.activo " +
            "ORDER BY totalResenas DESC",
            countQuery = "SELECT COUNT(*) FROM (SELECT u.id FROM usuario u GROUP BY u.id) AS sub",
            nativeQuery = true)
    Page<Object[]> findUsuariosConMasResenasPaginado(Pageable pageable);

    // ══════════════════════════════════════
    // 🎯 MÉTODOS HELPER - Conversión Object[] → DTO
    // ══════════════════════════════════════

    /**
     * Convierte una fila Object[] a UsuarioResenaDTO
     * Los índices corresponden al SELECT de las queries nativas:
     * [0] usuarioId, [1] nombreCompleto, [2] email, [3] totalResenas,
     * [4] puntuacionPromedio, [5] ultimaResena, [6] fechaRegistro, [7] activo
     */
    default UsuarioResenaDTO mapToUsuarioResenaDTO(Object[] row) {
        return new UsuarioResenaDTO(
                row[0] != null ? ((Number) row[0]).longValue() : null,           // usuarioId
                row[1] != null ? (String) row[1] : null,                         // nombreCompleto
                row[2] != null ? (String) row[2] : null,                         // email
                row[3] != null ? ((Number) row[3]).longValue() : 0L,             // totalResenas
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0,          // puntuacionPromedio
                row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null,  // ultimaResena
                row[6] != null ? ((java.sql.Timestamp) row[6]).toLocalDateTime() : null,  // fechaRegistro
                row[7] != null ? ((Number) row[7]).intValue() == 1 : false       // activo
        );
    }

    /**
     * Wrapper paginado con conversión automática a DTO
     */
    default Page<UsuarioResenaDTO> findUsuariosConMasResenasDTO(Pageable pageable) {
        return findUsuariosConMasResenasPaginado(pageable)
                .map(this::mapToUsuarioResenaDTO);
    }

    /**
     * Wrapper sin paginación con conversión automática a DTO
     */
    default List<UsuarioResenaDTO> findAllUsuariosConMasResenasDTO() {
        return findUsuariosConMasResenas().stream()
                .map(this::mapToUsuarioResenaDTO)
                .collect(Collectors.toList());
    }
}