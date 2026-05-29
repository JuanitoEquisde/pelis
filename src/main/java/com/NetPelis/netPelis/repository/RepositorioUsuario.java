package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByActivoTrue();
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:id IS NULL OR u.id = :id) AND " +
            "(:nombre IS NULL OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:rol IS NULL OR u.rol = :rol) AND " +
            "(:activo IS NULL OR u.activo = :activo) " +
            "ORDER BY u.fechaRegistro DESC")
    List<Usuario> buscarUsuarios(@Param("id") Long id,
                                 @Param("nombre") String nombre,
                                 @Param("email") String email,
                                 @Param("rol") RolUsuario rol,
                                 @Param("activo") Boolean activo);

    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombre);
    List<Usuario> findByEmailContainingIgnoreCase(String email);
}