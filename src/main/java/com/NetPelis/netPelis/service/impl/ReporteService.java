package com.NetPelis.netPelis.service.impl;

import com.NetPelis.netPelis.dto.UsuarioResenaDTO;
import com.NetPelis.netPelis.repository.RepositorioResena;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final RepositorioResena repositorioResena;

    /**
     * ✅ Convierte Object[] a UsuarioResenaDTO
     */
    private UsuarioResenaDTO convertirADTO(Object[] row) {
        return new UsuarioResenaDTO(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                row[1] != null ? (String) row[1] : null,
                row[2] != null ? (String) row[2] : null,
                row[3] != null ? ((Number) row[3]).longValue() : 0L,
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0,
                row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null,
                row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null,
                row[7] != null ? row[7] instanceof Boolean ? (Boolean) row[7] : ((Number) row[7]).intValue() == 1 : false
        );
    }

    /**
     * ✅ Obtener todos los usuarios con más reseñas
     */
    public List<UsuarioResenaDTO> getAllUsuariosConResenas() {
        return repositorioResena.findUsuariosConMasResenas()  // ✅ CAMBIADO: Sin "Raw"
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Obtener usuarios con paginación
     */
    public Page<UsuarioResenaDTO> getUsuariosConMasResenas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> pageRaw = repositorioResena.findUsuariosConMasResenasPaginado(pageable);

        List<UsuarioResenaDTO> dtos = pageRaw.getContent()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pageRaw.getTotalElements());
    }

    /**
     * ✅ Exportar reporte a Excel
     */
    public byte[] exportarAExcel() throws IOException {
        List<UsuarioResenaDTO> usuarios = getAllUsuariosConResenas();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Usuarios Activos");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ranking", "ID Usuario", "Nombre Completo", "Email",
                    "Total Reseñas", "Puntuación Promedio"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (UsuarioResenaDTO usuario : usuarios) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(usuario.getUsuarioId() != null ? usuario.getUsuarioId() : 0);
                row.createCell(2).setCellValue(usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : "");
                row.createCell(3).setCellValue(usuario.getEmail() != null ? usuario.getEmail() : "");
                row.createCell(4).setCellValue(usuario.getTotalResenas() != null ? usuario.getTotalResenas() : 0);
                row.createCell(5).setCellValue(usuario.getPuntuacionPromedio() != null ? usuario.getPuntuacionPromedio() : 0.0);
                rowNum++;
            }

            // Auto ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}