package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearMensualidadInput {
    private String planTratamientoId;
    private String pacienteId;
    /** Fecha ISO (yyyy-MM-dd) del primer día del mes que cubre la mensualidad. */
    private String periodo;
    private BigDecimal monto;
}
