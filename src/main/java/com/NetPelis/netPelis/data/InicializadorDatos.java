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
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!repositorioUsuario.existsByEmail("admin@netpelis.com")) {
            Usuario admin = new Usuario();
            admin.setNombreCompleto("Administrador NetPelis");
            admin.setEmail("admin@netpelis.com");
            admin.setContrasenaHash(encoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMIN);

            repositorioUsuario.save(admin);
        }

        if (!repositorioUsuario.existsByEmail("cliente@netpelis.com")) {
            Usuario cliente = new Usuario();
            cliente.setNombreCompleto("Usuario Demo");
            cliente.setEmail("cliente@netpelis.com");
            cliente.setContrasenaHash(encoder.encode("cliente123"));
            cliente.setRol(RolUsuario.CLIENTE);
            repositorioUsuario.save(cliente);
        }
    }
}