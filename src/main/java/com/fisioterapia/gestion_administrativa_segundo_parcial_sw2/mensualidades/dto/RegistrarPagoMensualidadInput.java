package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto;

import lombok.Data;

@Data
public class RegistrarPagoMensualidadInput {
    private String mensualidadId;
    private String empleadoId;
    private String metodoPago;
}
