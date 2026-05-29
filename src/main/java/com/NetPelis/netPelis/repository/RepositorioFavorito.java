package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioFavorito extends JpaRepository<Favorito, Long> {
    long countByUsuarioId(Long usuarioId);

    List<Favorito> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    void deleteByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);
}