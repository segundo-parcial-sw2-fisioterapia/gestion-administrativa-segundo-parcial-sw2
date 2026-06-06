package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensualidadEnriquecida {
    private Long id;
    private Long planTratamientoId;
    private Long pacienteId;
    private String periodo;
    private BigDecimal monto;
    private String estado;
    /** Convertido a String ISO-8601 para coincidir con el schema: fechaCreacion: String */
    private String fechaCreacion;
    private PersonaInfo paciente;
}
