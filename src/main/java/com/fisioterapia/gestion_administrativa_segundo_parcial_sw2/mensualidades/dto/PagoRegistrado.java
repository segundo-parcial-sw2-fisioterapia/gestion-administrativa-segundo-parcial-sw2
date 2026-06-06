package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoRegistrado {
    private MensualidadDto mensualidad;
    private FacturaEnriquecida factura;
}
