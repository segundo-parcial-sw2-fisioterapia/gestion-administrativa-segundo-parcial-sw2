package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.MensualidadEnriquecida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensualidadesPaginadasEnriquecidas {
    private List<MensualidadEnriquecida> contenido;
    private PaginaInfo paginaInfo;
}
