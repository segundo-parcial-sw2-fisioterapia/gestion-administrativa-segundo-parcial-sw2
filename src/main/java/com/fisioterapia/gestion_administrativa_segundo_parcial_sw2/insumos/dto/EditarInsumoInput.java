package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.CategoriaInsumo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EditarInsumoInput {
    private String nombre;
    private CategoriaInsumo categoria;
    private Integer stockActual;
    private Integer stockMinimo;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private Boolean activo;
}
