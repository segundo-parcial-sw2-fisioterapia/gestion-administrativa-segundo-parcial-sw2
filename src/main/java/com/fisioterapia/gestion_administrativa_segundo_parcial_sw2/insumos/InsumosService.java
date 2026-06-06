package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.dto.CrearInsumoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.dto.EditarInsumoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsumosService {

    private final InsumosRepository insumosRepository;

    /**
     * Lista insumos con paginación, ordenados por nombre.
     *
     * @param pagina Parámetros de paginación.
     * @return Página de insumos con metadatos.
     */
    public InsumosPaginados listarInsumos(PaginaInput pagina) {
        Pageable p = toPageable(pagina, Sort.by("nombre").ascending());
        Page<Insumos> page = insumosRepository.findAll(p);
        return new InsumosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El insumo con el ID dado, o null si no existe. */
    public Insumos verInsumo(String id) {
        return insumosRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /**
     * Retorna insumos cuyo stock actual ha alcanzado o caído por debajo del mínimo.
     *
     * @return Lista de insumos con stock bajo.
     */
    public List<Insumos> listarInsumosConStockBajo() {
        return insumosRepository.findInsumosConStockBajo();
    }

    /**
     * Registra un nuevo insumo en el catálogo de inventario.
     *
     * @param input Datos del insumo.
     * @return El insumo creado.
     */
    @Transactional
    public Insumos crearInsumos(CrearInsumoInput input) {
        Insumos insumo = Insumos.builder()
                .nombre(input.getNombre())
                .categoria(input.getCategoria())
                .stockActual(input.getStockActual())
                .stockMinimo(input.getStockMinimo())
                .unidadMedida(input.getUnidadMedida())
                .precioUnitario(input.getPrecioUnitario())
                .build();
        return insumosRepository.save(insumo);
    }

    /**
     * Actualiza los datos de un insumo existente.
     *
     * @param id    ID del insumo.
     * @param input Campos a modificar.
     * @return El insumo actualizado.
     */
    @Transactional
    public Insumos editarInsumo(String id, EditarInsumoInput input) {
        Insumos insumo = insumosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + id));
        if (input.getNombre() != null) insumo.setNombre(input.getNombre());
        if (input.getCategoria() != null) insumo.setCategoria(input.getCategoria());
        if (input.getStockActual() != null) insumo.setStockActual(input.getStockActual());
        if (input.getStockMinimo() != null) insumo.setStockMinimo(input.getStockMinimo());
        if (input.getUnidadMedida() != null) insumo.setUnidadMedida(input.getUnidadMedida());
        if (input.getPrecioUnitario() != null) insumo.setPrecioUnitario(input.getPrecioUnitario());
        if (input.getActivo() != null) insumo.setActivo(input.getActivo());
        return insumosRepository.save(insumo);
    }

    /**
     * Elimina un insumo del catálogo por su ID.
     *
     * @param id ID del insumo a eliminar.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarInsumo(String id) {
        insumosRepository.deleteById(Long.parseLong(id));
        return true;
    }

    private Pageable toPageable(PaginaInput p, Sort sort) {
        int pagina = p != null ? Math.max(0, p.getPagina()) : 0;
        int tamano = p != null ? Math.min(100, Math.max(1, p.getTamano())) : 20;
        return PageRequest.of(pagina, tamano, sort);
    }
}
