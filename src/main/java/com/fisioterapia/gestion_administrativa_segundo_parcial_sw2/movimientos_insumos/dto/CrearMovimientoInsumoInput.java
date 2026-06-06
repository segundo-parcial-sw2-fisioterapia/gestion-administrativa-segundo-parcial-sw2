package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.TipoMovimiento;
import lombok.Data;

@Data
public class CrearMovimientoInsumoInput {
    private String insumoId;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private String registradoPor;
    private String observaciones;
}
