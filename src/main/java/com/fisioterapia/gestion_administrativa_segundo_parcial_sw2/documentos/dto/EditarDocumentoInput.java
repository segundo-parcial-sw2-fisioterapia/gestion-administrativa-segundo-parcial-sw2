package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.TipoDocumento;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.TipoEntidadDocumento;
import lombok.Data;

@Data
public class EditarDocumentoInput {
    private String nombre;
    private TipoDocumento tipo;
    private String urlS3;
    private String referenciaEntidad;
    private TipoEntidadDocumento tipoEntidad;
}
