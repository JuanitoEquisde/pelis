package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.service.ArchivoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArchivoStorageServiceImpl implements ArchivoStorageService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.url}")
    private String uploadUrl;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("✅ Directorio de uploads creado: " + uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + uploadPath, e);
        }
    }

    @Override
    public String guardarImagen(MultipartFile archivo, String tipo) throws IOException {
        if (archivo.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        // Validar que sea imagen
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Solo se permiten archivos de imagen. Recibido: " + contentType);
        }

        // Validar tamaño (10MB)
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new IOException("La imagen no puede superar los 10MB");
        }

        // Generar nombre único seguro
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + "." + extension;

        // Ruta completa de guardado
        Path rutaCompleta = Paths.get(uploadPath + nombreArchivo);

        // Guardar archivo (reemplazar si existe)
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

        // Retornar URL pública
        return uploadUrl + nombreArchivo;
    }

    @Override
    public void eliminarImagen(String urlImagen) throws IOException {
        if (urlImagen != null && !urlImagen.isEmpty() && urlImagen.startsWith(uploadUrl)) {
            String nombreArchivo = urlImagen.substring(uploadUrl.length());
            Path rutaArchivo = Paths.get(uploadPath + nombreArchivo);

            if (Files.exists(rutaArchivo)) {
                Files.delete(rutaArchivo);
                System.out.println("🗑️ Imagen eliminada: " + nombreArchivo);
            }
        }
    }

    @Override
    public String getUploadUrl() {
        return uploadUrl;
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "jpg";
        }
        String ext = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp" -> ext;
            default -> "jpg";
        };
    }
}