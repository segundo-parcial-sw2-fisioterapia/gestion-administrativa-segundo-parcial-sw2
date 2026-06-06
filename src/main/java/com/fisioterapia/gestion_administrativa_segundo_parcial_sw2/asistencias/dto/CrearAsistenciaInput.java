package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.EstadoAsistencia;
import lombok.Data;

@Data
public class CrearAsistenciaInput {
    private String empleadoId;
    private String fecha;
    private String horaEntrada;
    private String horaSalida;
    private EstadoAsistencia estado;
}
