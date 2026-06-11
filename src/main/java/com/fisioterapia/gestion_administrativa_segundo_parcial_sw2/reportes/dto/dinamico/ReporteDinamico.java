package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Resultado ejecutado de un reporte dinámico, listo para graficar en el frontend. */
@Data
@AllArgsConstructor
public class ReporteDinamico {
    private String titulo;
    private String etiquetaDimension;
    private String etiquetaMetrica;
    private List<FilaReporte> filas;
    private double total;
    private TipoGrafico visualizacionSugerida;
}
