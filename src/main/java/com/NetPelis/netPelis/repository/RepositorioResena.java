package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepositorioResena extends JpaRepository<Resena, Long> {

    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario ORDER BY r.fechaCreacion DESC LIMIT :limit")
    List<Resena> findRecientesConUsuario(@Param("limit") int limit);
    long countByUsuarioId(Long usuarioId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Resena> findByPeliculaIdOrderByFechaCreacionDesc(Long peliculaId);
}