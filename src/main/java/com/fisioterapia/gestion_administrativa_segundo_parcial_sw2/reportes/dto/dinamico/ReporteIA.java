package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta de un reporte generado a partir de un prompt en lenguaje natural.
 * Contiene la especificación que la IA interpretó, el reporte ejecutado de forma
 * determinista por el motor, y un resumen narrativo del resultado.
 */
@Data
@AllArgsConstructor
public class ReporteIA {
    private String prompt;
    private ReporteDinamicoInput interpretacion;
    private ReporteDinamico resultado;
    private String resumenNarrativo;
}
