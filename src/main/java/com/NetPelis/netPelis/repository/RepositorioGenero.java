package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioGenero extends JpaRepository<Genero, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Optional<Genero> findByNombre(String nombre);
}