package com.NetPelis.netPelis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuariosConMasResenasDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private Long totalResenas;
    private BigDecimal puntuacionPromedio;
    private Timestamp ultimaResena;
    private Timestamp fechaRegistro;

    /**
     * Constructor corto (5 parámetros)
     */
    public UsuariosConMasResenasDTO(Long id, String nombreCompleto, String email,
                                    Long totalResenas, BigDecimal puntuacionPromedio) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.totalResenas = totalResenas;
        this.puntuacionPromedio = puntuacionPromedio;
        this.ultimaResena = null;
        this.fechaRegistro = null;
    }
}