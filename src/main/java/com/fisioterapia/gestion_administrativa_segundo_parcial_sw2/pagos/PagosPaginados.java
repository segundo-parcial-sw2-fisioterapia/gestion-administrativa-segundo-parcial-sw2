package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagosPaginados {
    private List<Pagos> contenido;
    private PaginaInfo paginaInfo;
}
