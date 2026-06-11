package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Catálogo de una fuente: qué se puede agrupar y qué se puede agregar. */
@Data
@AllArgsConstructor
public class FuenteMeta {
    private FuenteReporte fuente;
    private String etiqueta;
    private boolean soportaAnio;
    private List<CampoMeta> dimensiones;
    private List<CampoMeta> numericos;
}
