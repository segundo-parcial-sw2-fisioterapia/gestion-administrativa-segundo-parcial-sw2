package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos personales básicos obtenidos del microservicio clinica.
 * Se enriquece en tiempo de ejecución al resolver consultas de Empleados.
 */
@Data
@NoArgsConstructor
public class PersonaInfo {
    private String nombre;
    private String apellido;
    private String ci;
    private String telefono;
}
