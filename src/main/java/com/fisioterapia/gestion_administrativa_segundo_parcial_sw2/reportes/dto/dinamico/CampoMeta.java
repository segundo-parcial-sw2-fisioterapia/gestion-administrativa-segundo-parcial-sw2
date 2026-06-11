package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Metadato de un campo (dimensión o numérico) consultable de una fuente. */
@Data
@AllArgsConstructor
public class CampoMeta {
    private String campo;
    private String etiqueta;
}
