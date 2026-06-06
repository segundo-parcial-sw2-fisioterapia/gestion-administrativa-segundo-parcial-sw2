package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActualizarTarifaInput {
    private BigDecimal precioMensual;
    private Long actualizadoPor;
}
