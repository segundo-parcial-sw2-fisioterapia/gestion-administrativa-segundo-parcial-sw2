package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EstadoLaboral;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.TipoContrato;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EditarEmpleadoInput {
    private String cargo;
    private String especialidad;
    private BigDecimal salarioBase;
    private TipoContrato tipoContrato;
    private String fechaBaja;
    private EstadoLaboral estadoLaboral;
}
