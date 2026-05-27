package com.NetPelis.netPelis.config;

import com.NetPelis.netPelis.service.ServicioDetallesUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final ServicioDetallesUsuario servicioDetallesUsuario;

    public SeguridadConfig(ServicioDetallesUsuario servicioDetallesUsuario) {
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Bean
    public PasswordEncoder codificadorContraseña() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/imagenes/**").permitAll()
                        .requestMatchers("/admin/**", "/admin-dashboard").hasRole("ADMIN")
                        .requestMatchers("/cliente/**", "/cliente-dashboard").hasRole("CLIENTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/procesar-login")
                        .usernameParameter("email")
                        .passwordParameter("contrasena")
                        .defaultSuccessUrl("/redireccionar-segun-rol", true)
                        .failureUrl("/?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/cerrar-sesion")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}