package com.NetPelis.netPelis.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resena", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resena_usuario_pelicula", columnNames = {"usuario_id", "pelicula_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "usuario", "pelicula"})  // ← ← ← AGREGAR ESTO

@EqualsAndHashCode(exclude = {"usuario", "pelicula"}) // ✅ Evitar bucle infinito
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pelicula_id", nullable = false)
    private Pelicula pelicula;

    // ✅ BigDecimal para DECIMAL(3,1) en MySQL
    @Column(name = "puntuacion", precision = 3, scale = 1, columnDefinition = "DECIMAL(3,1)")
    private BigDecimal puntuacion;

    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "es_recomendada")
    private Boolean esRecomendada = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;



}