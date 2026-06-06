package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearFacturaInput {
    private String pacienteId;
    private String numeroFactura;
    private String fechaEmision;
    private BigDecimal montoTotal;
    private String mensualidadId;
    private String concepto;
    private String empleadoId;
    private String metodoPago;
    private String urlDocumento;
}
