package com.NetPelis.netPelis;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Clase utilitaria para generar INSERTs de prueba con contraseñas encriptadas en BCrypt.
 * Ejecútala como aplicación Java normal (clic derecho -> Run).
 */
public class GeneradorInsertsSQL {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 1️⃣ Datos ADMIN
        String adminNombre = "Administrador NetPelis";
        String adminEmail  = "admin@netpelis.com";
        String adminPass   = "admin123";
        String adminHash   = encoder.encode(adminPass);

        // 2️⃣ Datos CLIENTE
        String clienteNombre = "Usuario Demo";
        String clienteEmail  = "cliente@netpelis.com";
        String clientePass   = "cliente123";
        String clienteHash   = encoder.encode(clientePass);

        // 🖨️ Generar INSERTs compatibles con tu tabla 'usuario'
        String insertAdmin = String.format(
                "INSERT INTO usuario (nombre_completo, email, contrasena_hash, rol, activo) VALUES ('%s', '%s', '%s', 'ADMIN', TRUE);",
                adminNombre, adminEmail, adminHash
        );

        String insertCliente = String.format(
                "INSERT INTO usuario (nombre_completo, email, contrasena_hash, rol, activo) VALUES ('%s', '%s', '%s', 'CLIENTE', TRUE);",
                clienteNombre, clienteEmail, clienteHash
        );

        // 📤 Imprimir resultado listo para copiar
        System.out.println("\n🔐 === COPIA Y EJECUTA EN TU CLIENTE MYSQL ===\n");
        System.out.println("--  Usuario Administrador");
        System.out.println(insertAdmin);
        System.out.println("\n-- 🎟️ Usuario Cliente");
        System.out.println(insertCliente);
        System.out.println("\n✅ Credenciales de prueba:");
        System.out.println("   ADMIN:   " + adminEmail + " | " + adminPass);
        System.out.println("   CLIENTE: " + clienteEmail + " | " + clientePass);
        System.out.println("==================================================\n");
    }
}