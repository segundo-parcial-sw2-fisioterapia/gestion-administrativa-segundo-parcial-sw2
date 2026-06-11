package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Resumen del estado de cobranza de las mensualidades del período. */
@Data
@AllArgsConstructor
public class ResumenMensualidades {
    private int pagadas;
    private int pendientes;
    private int vencidas;
    private double montoPagado;
    private double montoPendiente;
}
