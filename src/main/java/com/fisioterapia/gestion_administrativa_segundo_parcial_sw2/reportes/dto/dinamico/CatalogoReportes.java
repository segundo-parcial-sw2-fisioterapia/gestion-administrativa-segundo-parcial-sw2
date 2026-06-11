package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Catálogo completo de fuentes/dimensiones/métricas permitidas en reportes dinámicos. */
@Data
@AllArgsConstructor
public class CatalogoReportes {
    private List<FuenteMeta> fuentes;
}
