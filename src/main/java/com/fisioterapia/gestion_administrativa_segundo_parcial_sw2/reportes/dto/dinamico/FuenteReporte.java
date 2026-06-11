package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

/**
 * Fuentes de datos disponibles para el motor de reportes dinámicos.
 * Cada valor está mapeado en {@code ReporteDinamicoService} a un repositorio real
 * y a su catálogo de dimensiones/métricas permitidas (whitelist de seguridad).
 */
public enum FuenteReporte {
    FACTURAS,
    MENSUALIDADES,
    PAGOS,
    EMPLEADOS,
    INSUMOS,
    MOVIMIENTOS_INSUMOS,
    TARIFAS
}
