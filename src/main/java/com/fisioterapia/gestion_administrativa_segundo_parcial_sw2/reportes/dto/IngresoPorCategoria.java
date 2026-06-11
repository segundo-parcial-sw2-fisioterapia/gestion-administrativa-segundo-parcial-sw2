package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Total facturado y cantidad de facturas agrupadas por una categoría (ej. método de pago). */
@Data
@AllArgsConstructor
public class IngresoPorCategoria {
    private String categoria;
    private double total;
    private int cantidad;
}
