package com.NetPelis.netPelis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.url}")
    private String uploadUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar handler para imágenes locales subidas
        Path path = Paths.get(uploadPath);
        registry.addResourceHandler(uploadUrl + "**")
                .addResourceLocations("file:" + path.toAbsolutePath() + "/");

        // Permitir cargar imágenes externas (TMDB, placeholders, etc.)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("https://image.tmdb.org/",
                        "https://via.placeholder.com/",
                        "https://i.pinimg.com/");

        System.out.println("✅ Uploads configurados: " + uploadUrl + " → " + path);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}