package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Ingresos facturados de un mes específico dentro de un reporte anual. */
@Data
@AllArgsConstructor
public class IngresoMensual {
    private int mes;            // 1-12
    private String etiqueta;    // "Ene", "Feb", ...
    private double totalIngresos;
    private int totalFacturas;
}
