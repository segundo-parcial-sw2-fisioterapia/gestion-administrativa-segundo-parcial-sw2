package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Especificación de un reporte dinámico. Es el contrato compartido por:
 *  - el constructor manual del frontend (selects),
 *  - y la capa de IA, que traduce un prompt en lenguaje natural a esta estructura.
 *
 * El backend valida cada campo contra el catálogo whitelisteado antes de ejecutar,
 * por lo que la IA nunca puede consultar tablas ni campos no permitidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDinamicoInput {

    /** Fuente de datos (entidad) sobre la que se construye el reporte. */
    private FuenteReporte fuente;

    /** Función de agregación: CONTEO, SUMA o PROMEDIO. */
    private MetricaReporte metrica;

    /** Campo numérico a agregar. Requerido para SUMA y PROMEDIO; ignorado en CONTEO. */
    private String campoMetrica;

    /** Dimensión por la que se agrupan los resultados (ej. "mes", "metodoPago", "estado"). */
    private String agruparPor;

    /** Filtro opcional por año sobre la fecha primaria de la fuente. */
    private Integer anio;

    /** Dimensión opcional sobre la que aplicar un filtro de igualdad. */
    private String filtroCampo;

    /** Valor del filtro de igualdad (comparación textual contra la dimensión). */
    private String filtroValor;

    /** Rango de fecha predefinido (ej. "hoy", "ultima_semana", "ultimo_mes"). */
    private String rangoFecha;
}
