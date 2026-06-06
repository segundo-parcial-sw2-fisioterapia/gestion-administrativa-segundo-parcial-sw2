package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearPagoInput {
    private String facturaId;
    private String fechaPago;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private String referencia;
    private String registradoPor;
}
