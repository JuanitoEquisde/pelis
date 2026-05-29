package com.NetPelis.netPelis.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ArchivoStorageService {

    /**
     * Guarda una imagen y retorna su URL pública
     */
    String guardarImagen(MultipartFile archivo, String tipo) throws IOException;

    /**
     * Elimina una imagen del sistema de archivos
     */
    void eliminarImagen(String urlImagen) throws IOException;

    /**
     * Obtiene la URL base de uploads
     */
    String getUploadUrl();
}