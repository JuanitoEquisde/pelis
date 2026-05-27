package com.NetPelis.netPelis.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface ServicioDetallesUsuario extends UserDetailsService {
    UserDetails cargarUsuarioPorEmail(String email);
}