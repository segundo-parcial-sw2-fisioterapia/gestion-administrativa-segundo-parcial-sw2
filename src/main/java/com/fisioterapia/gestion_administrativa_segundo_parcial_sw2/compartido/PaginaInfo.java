package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaInfo {
    private int totalElementos;
    private int totalPaginas;
    private int paginaActual;
    private int tamano;
    private boolean primero;
    private boolean ultimo;

    /**
     * Construye PaginaInfo a partir de un Page de Spring Data.
     * Convierte totalElements (long) a int de forma segura para este dominio.
     *
     * @param page Página resultante de un repositorio Spring Data.
     */
    public static PaginaInfo de(Page<?> page) {
        return new PaginaInfo(
                (int) page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
