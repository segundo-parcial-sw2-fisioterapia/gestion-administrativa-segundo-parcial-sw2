package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.DiaSemana;
import lombok.Data;

@Data
public class CrearTurnoInput {
    private String empleadoId;
    private DiaSemana diaSemana;
    private String horaInicio;
    private String horaFin;
}
