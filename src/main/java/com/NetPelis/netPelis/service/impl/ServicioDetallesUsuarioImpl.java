package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import com.NetPelis.netPelis.service.ServicioDetallesUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ServicioDetallesUsuarioImpl implements ServicioDetallesUsuario {

    private final RepositorioUsuario repositorioUsuario;

    @Override
    public UserDetails cargarUsuarioPorEmail(String email) {
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // ✅ CORRECCIÓN: Usar .name() para obtener el string del enum
        String rolConPrefijo = "ROLE_" + usuario.getRol().name();

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getContrasenaHash(),
                Collections.singletonList(new SimpleGrantedAuthority(rolConPrefijo))
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return cargarUsuarioPorEmail(username);
    }
}