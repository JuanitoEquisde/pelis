package com.NetPelis.netPelis.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "genero")
@EqualsAndHashCode(exclude = {"peliculas"}) // ✅ Excluir relación bidireccional
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @ManyToMany(mappedBy = "generos")
    private Set<Pelicula> peliculas = new HashSet<>();
}