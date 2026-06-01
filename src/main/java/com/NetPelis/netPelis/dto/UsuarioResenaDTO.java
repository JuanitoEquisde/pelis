package com.NetPelis.netPelis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResenaDTO {
    private Long usuarioId;
    private String nombreCompleto;
    private String email;
    private Long totalResenas;
    private Double puntuacionPromedio;
}