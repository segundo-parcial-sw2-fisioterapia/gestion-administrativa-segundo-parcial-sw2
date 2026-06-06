package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.Insumos;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.InsumosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.dto.CrearMovimientoInsumoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientosInsumosService {

    private final MovimientosInsumosRepository movimientosRepository;
    private final InsumosRepository insumosRepository;

    /** Lista movimientos paginados, ordenados por fecha descendente. */
    public MovimientosInsumosPaginados listarMovimientosInsumos(PaginaInput pagina) {
        int p = pagina != null ? Math.max(0, pagina.getPagina()) : 0;
        int t = pagina != null ? Math.min(100, Math.max(1, pagina.getTamano())) : 20;
        Page<MovimientosInsumos> page = movimientosRepository.findAll(
                PageRequest.of(p, t, Sort.by("fecha").descending()));
        return new MovimientosInsumosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El movimiento con el ID dado, o null si no existe. */
    public MovimientosInsumos verMovimientoInsumo(String id) {
        return movimientosRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista el historial de movimientos de un insumo específico. */
    public List<MovimientosInsumos> listarMovimientosPorInsumo(String insumoId) {
        return movimientosRepository.findByInsumoIdOrderByFechaDesc(Long.parseLong(insumoId));
    }

    /**
     * Registra una entrada o salida de inventario y actualiza el stock del insumo.
     * Las entradas incrementan el stock; las salidas lo decrementan.
     *
     * @param input Datos del movimiento.
     * @return El movimiento registrado.
     */
    @Transactional
    public MovimientosInsumos crearMovimientosInsumos(CrearMovimientoInsumoInput input) {
        Insumos insumo = insumosRepository.findById(Long.parseLong(input.getInsumoId()))
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + input.getInsumoId()));

        if (input.getTipo() == TipoMovimiento.entrada) {
            insumo.setStockActual(insumo.getStockActual() + input.getCantidad());
        } else {
            if (insumo.getStockActual() < input.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para registrar la salida");
            }
            insumo.setStockActual(insumo.getStockActual() - input.getCantidad());
        }
        insumosRepository.save(insumo);

        return movimientosRepository.save(MovimientosInsumos.builder()
                .insumoId(insumo.getId())
                .tipo(input.getTipo())
                .cantidad(input.getCantidad())
                .registradoPor(input.getRegistradoPor() != null ? Long.parseLong(input.getRegistradoPor()) : null)
                .observaciones(input.getObservaciones())
                .build());
    }

    /**
     * Elimina un registro de movimiento por su ID.
     *
     * @param id ID del movimiento.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarMovimientoInsumo(String id) {
        movimientosRepository.deleteById(Long.parseLong(id));
        return true;
    }
}
