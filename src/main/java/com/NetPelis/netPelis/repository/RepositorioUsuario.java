package com.NetPelis.netPelis.repository;

import com.NetPelis.netPelis.dto.UsuariosConMasResenasDTO;
import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    // ══════════════════════════════════════
    // MÉTODOS EXISTENTES (NO SE BORRAN)
    // ══════════════════════════════════════

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

    // ══════════════════════════════════════
    // 🆕 MÉTODOS PARA REPORTE USUARIOS CON MÁS RESEÑAS
    // ✅ Usan la vista v_usuarios_activos de la BD
    // ✅ Retornan directamente UsuariosConMasResenasDTO
    // ══════════════════════════════════════

    /**
     * Obtiene usuarios con más reseñas (con paginación)
     * Usa la vista v_usuarios_activos
     */
    @Query(value = "SELECT usuario_id AS id, " +
            "nombre_completo AS nombreCompleto, " +
            "email AS email, " +
            "total_resenas AS totalResenas, " +
            "puntuacion_promedio AS puntuacionPromedio, " +
            "ultima_resena AS ultimaResena, " +
            "fecha_registro AS fechaRegistro " +
            "FROM v_usuarios_activos " +
            "ORDER BY total_resenas DESC",
            countQuery = "SELECT COUNT(*) FROM v_usuarios_activos",
            nativeQuery = true)
    Page<UsuariosConMasResenasDTO> findUsuariosConMasResenas(Pageable pageable);

    /**
     * Busca usuarios con más reseñas filtrando por nombre o email
     */
    @Query(value = "SELECT usuario_id AS id, " +
            "nombre_completo AS nombreCompleto, " +
            "email AS email, " +
            "total_resenas AS totalResenas, " +
            "puntuacion_promedio AS puntuacionPromedio, " +
            "ultima_resena AS ultimaResena, " +
            "fecha_registro AS fechaRegistro " +
            "FROM v_usuarios_activos " +
            "WHERE LOWER(nombre_completo) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(email) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "ORDER BY total_resenas DESC",
            countQuery = "SELECT COUNT(*) FROM v_usuarios_activos " +
                    "WHERE LOWER(nombre_completo) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
                    "OR LOWER(email) LIKE LOWER(CONCAT('%', :busqueda, '%'))",
            nativeQuery = true)
    Page<UsuariosConMasResenasDTO> findUsuariosConMasResenasConBusqueda(
            @Param("busqueda") String busqueda,
            Pageable pageable);

    /**
     * Cuenta total de usuarios con reseñas
     */
    @Query(value = "SELECT COUNT(*) FROM v_usuarios_activos", nativeQuery = true)
    long countUsuariosConResenas();

    /**
     * Cuenta usuarios con reseñas que coinciden con la búsqueda
     */
    @Query(value = "SELECT COUNT(*) FROM v_usuarios_activos " +
            "WHERE LOWER(nombre_completo) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(email) LIKE LOWER(CONCAT('%', :busqueda, '%'))",
            nativeQuery = true)
    long countUsuariosConResenasConBusqueda(@Param("busqueda") String busqueda);

    /**
     * Método helper para convertir Timestamp a String formateado
     * Útil para mostrar en el HTML
     */
    default String formatearFecha(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return timestamp.toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}