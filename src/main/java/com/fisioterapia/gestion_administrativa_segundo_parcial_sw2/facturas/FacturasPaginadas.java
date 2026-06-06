package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FacturasPaginadas {
    private List<Facturas> contenido;
    private PaginaInfo paginaInfo;
}
