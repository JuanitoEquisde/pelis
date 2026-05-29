package com.NetPelis.netPelis;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Genera INSERTs SQL para 20 usuarios de prueba con contraseñas encriptadas en BCrypt.
 * Ejecútala como aplicación Java normal (clic derecho -> Run).
 */
public class GeneradorInsertsSQL {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("\n🔐 === GENERANDO 20 USUARIOS DE PRUEBA ===\n");
        System.out.println("-- ============================================");
        System.out.println("-- COPIA Y EJECUTA ESTOS INSERTS EN MYSQL");
        System.out.println("-- ============================================\n");

        // Generar 20 usuarios
        for (int i = 1; i <= 20; i++) {
            String nombre = "Usuario " + i;
            String email = "usuario" + i + "@netpelis.com";
            String password = "usuario" + i + "123";
            String hash = encoder.encode(password);

            // Los primeros 3 son ADMIN, el resto CLIENTE
            String rol = (i <= 3) ? "ADMIN" : "CLIENTE";

            String insert = String.format(
                    "INSERT INTO usuario (nombre_completo, email, contrasena_hash, rol, activo) " +
                            "VALUES ('%s', '%s', '%s', '%s', TRUE);",
                    nombre, email, hash, rol
            );

            System.out.println("-- Usuario " + i + " (" + rol + ")");
            System.out.println(insert);
            System.out.println();
        }

        // Imprimir credenciales
        System.out.println("\n✅ ============================================");
        System.out.println("✅ CREDENCIALES DE PRUEBA");
        System.out.println("✅ ============================================");
        System.out.println();
        System.out.println("📋 ADMINISTRADORES (3):");
        for (int i = 1; i <= 3; i++) {
            System.out.println("   Email:    usuario" + i + "@netpelis.com");
            System.out.println("   Password: usuario" + i + "123");
            System.out.println();
        }

        System.out.println("📋 CLIENTES (17):");
        for (int i = 4; i <= 20; i++) {
            System.out.println("   Email:    usuario" + i + "@netpelis.com");
            System.out.println("   Password: usuario" + i + "123");
            if (i <= 6) System.out.println(); // Solo mostrar primeros 3 clientes
        }
        System.out.println("   ... (y 14 más)");

        System.out.println("\n✅ ============================================");
        System.out.println("✅ Total: 20 usuarios generados");
        System.out.println("✅ ============================================\n");
    }
}