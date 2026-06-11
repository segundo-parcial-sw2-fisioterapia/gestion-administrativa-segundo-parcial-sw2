package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Un grupo del reporte: la etiqueta de la dimensión y el valor agregado de la métrica. */
@Data
@AllArgsConstructor
public class FilaReporte {
    private String etiqueta;
    private double valor;
}
