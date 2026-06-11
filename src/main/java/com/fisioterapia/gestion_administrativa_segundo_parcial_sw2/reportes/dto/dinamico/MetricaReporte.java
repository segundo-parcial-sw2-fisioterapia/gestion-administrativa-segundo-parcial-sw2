package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

/**
 * Función de agregación aplicada a cada grupo del reporte dinámico.
 * CONTEO no requiere campoMetrica; SUMA y PROMEDIO operan sobre un campo numérico.
 */
public enum MetricaReporte {
    CONTEO,
    SUMA,
    PROMEDIO
}
