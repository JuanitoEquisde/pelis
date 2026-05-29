package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> buscarUsuarios(Long id, String nombre, String email, RolUsuario rol, Boolean activo) {
        return repositorioUsuario.buscarUsuarios(id, nombre, email, rol, activo);
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return repositorioUsuario.findById(id);
    }

    public Optional<Usuario> obtenerPorEmail(String email) {
        return repositorioUsuario.findByEmail(email);
    }

    public Usuario guardarUsuario(Usuario usuario, String passwordPlano) {
        // Si es nuevo usuario, establecer fecha de registro
        if (usuario.getId() == null) {
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setActivo(true);
        }

        // Si se proporciona contraseña, encriptarla
        if (passwordPlano != null && !passwordPlano.isEmpty()) {
            usuario.setContrasenaHash(passwordEncoder.encode(passwordPlano));
        }

        return repositorioUsuario.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        // Soft delete: desactivar en lugar de eliminar
        repositorioUsuario.findById(id).ifPresent(usuario -> {
            usuario.setActivo(false);
            repositorioUsuario.save(usuario);
        });
    }

    public void cambiarEstadoUsuario(Long id, boolean activo) {
        repositorioUsuario.findById(id).ifPresent(usuario -> {
            usuario.setActivo(activo);
            repositorioUsuario.save(usuario);
        });
    }

    public long contarPorRol(RolUsuario rol) {
        return repositorioUsuario.findAll().stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .count();
    }
}