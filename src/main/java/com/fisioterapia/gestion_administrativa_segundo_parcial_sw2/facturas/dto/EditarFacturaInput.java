package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.EstadoFactura;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EditarFacturaInput {
    private String pacienteId;
    private String numeroFactura;
    private String fechaEmision;
    private BigDecimal montoTotal;
    private EstadoFactura estado;
    private String mensualidadId;
    private String concepto;
    private String empleadoId;
    private String metodoPago;
    private String urlDocumento;
}
