package com.NetPelis.netPelis.data;

import com.NetPelis.netPelis.entity.RolUsuario;
import com.NetPelis.netPelis.entity.Usuario;
import com.NetPelis.netPelis.repository.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InicializadorDatos implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crear ADMIN si no existe
        if (!repositorioUsuario.existsByEmail("admin@netpelis.com")) {
            Usuario admin = new Usuario();
            admin.setNombreCompleto("Administrador NetPelis");
            admin.setEmail("admin@netpelis.com");
            admin.setContrasenaHash(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMIN);
            admin.setActivo(true);
            repositorioUsuario.save(admin);
            System.out.println(" Usuario ADMIN creado: admin@netpelis.com / admin123");
        }

        // Crear CLIENTE si no existe
        if (!repositorioUsuario.existsByEmail("cliente@netpelis.com")) {
            Usuario cliente = new Usuario();
            cliente.setNombreCompleto("Usuario Demo");
            cliente.setEmail("cliente@netpelis.com");
            cliente.setContrasenaHash(passwordEncoder.encode("cliente123"));
            cliente.setRol(RolUsuario.CLIENTE);
            cliente.setActivo(true);
            repositorioUsuario.save(cliente);
            System.out.println(" Usuario CLIENTE creado: cliente@netpelis.com / cliente123");
        }
    }
}