package com.NetPelis.netPelis.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuario")
// ✅ Usuario NO tiene colecciones bidireccionales definidas,
// pero si las agregas en el futuro, usa: @EqualsAndHashCode(exclude = {"resenas", "favoritos", ...})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol = RolUsuario.CLIENTE;

    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // ℹ️ Si en el futuro agregas colecciones como:
    // @OneToMany(mappedBy = "usuario") private Set<Resena> resenas;
    // Entonces DEBES agregar: @EqualsAndHashCode(exclude = {"resenas", "favoritos", "comentarios"})
}