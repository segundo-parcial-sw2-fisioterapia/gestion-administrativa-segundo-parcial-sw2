package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Reporte financiero consolidado de un año: ingresos por mes, por método de pago y cobranza. */
@Data
@AllArgsConstructor
public class ReporteFinanciero {
    private int anio;
    private double totalIngresos;
    private int totalFacturas;
    private double ticketPromedio;
    private List<IngresoMensual> ingresosPorMes;
    private List<IngresoPorCategoria> ingresosPorMetodoPago;
    private ResumenMensualidades mensualidades;
}
